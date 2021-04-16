package springboot.scheduletask;

import com.sun.management.OperatingSystemMXBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import springboot.modal.vo.LogVo;
import springboot.service.ILogService;
import springboot.service.IMailService;
import springboot.service.IMetaService;
import springboot.service.IUcmlService;
import springboot.service.impl.UcmlService;
import springboot.util.DateKit;

import javax.annotation.Resource;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * @author tangj
 * @date 2018/5/2 22:59
 */
@Component
public class ScheduleTask {

    @Resource
    ILogService logService;

    @Resource
    IMailService mailService;
    
    @Autowired
    private IUcmlService ucml;

    @Value("${spring.mail.username}")
    private String mailTo;

    @Scheduled(fixedRate = 86400000)
    public void process(){
        StringBuffer result = new StringBuffer();
        long totalMemory = Runtime.getRuntime().totalMemory();
        result.append("使用的总内存为："+totalMemory/(1024*1024)+"MB").append("\n");
        result.append("内存使用率为："+getMemery()).append("\n");
        List<LogVo> logVoList = logService.getLogs(0,5);
        for (LogVo logVo:logVoList){
            result.append(" 时间: ").append(DateKit.formatDateByUnixTime(logVo.getCreated()));
            result.append(" 操作: ").append(logVo.getAction());
            result.append(" IP： ").append(logVo.getIp()).append("\n");
        }
        mailService.sendSimpleEmail(mailTo,"博客系统运行情况",result.toString());
    }
    
    @Scheduled(cron="0 10 16 * * ? ")
    public void selfProcess() {
    	System.out.println(Thread.currentThread().getName()+"执行了一次定时任务，当前时间："+ DateKit.dateFormat(new Date()));
    	//mailService.sendSimpleEmail(mailTo, "每日8点16点提示消息", "做个测试  嘎嘎");
    }
    
    //每天上午7:10
    @Scheduled(cron="0 10 7 * * ? ")
    public void ucmlScheduled() {
    	StringBuffer result = new StringBuffer();
    	Hashtable<Object, Object> ucmlMap = getUcml(ucml);
    	int size = ucmlMap.size();
    	Set<Entry<Object, Object>> entrySet = ucmlMap.entrySet();
    	if(size >0) {
    		for (Entry<Object, Object> entry : entrySet) {
				Object key = entry.getKey();
				Object val = entry.getValue();
				result.append(" 操作: ").append(key);
	            result.append(" 结果： ").append(val).append("\n");
			}
    	}
    	mailService.sendSimpleEmail(mailTo,"R1系统定时任务执行情况",result.toString());
    }
    
    
    public static Hashtable<Object, Object> getUcml(IUcmlService ucmlS){
    	Hashtable<Object, Object> hashtable = new Hashtable<Object, Object>();
    	
    	//1、异常项目经理点击太快 物料状态未更改 查询 （实际上是中间被改了am_tmold表的结构  以及处理逻辑中的关键字段也被做他用——猜测  已经更改了相应的逻辑）
    	String firstSql = " SELECT distinct b.Code FROM AM_TMATUSELIST a\n" + 
    			"left join AM_TERRORREPORT b on a.AM_TERRORREPORT_FK = b.AM_TERRORREPORTOID\n" + 
    			"left join TaskTicket c on c.InstanceID = b.InstanceID\n" + 
    			"where a.ModifyReason='异常导入'\n" + 
    			"and a.CreateTime > '2020-1-1'\n" + 
    			"and a.MatState = '6'\n" + 
    			"and c.ActivityID = 'AC_12430VER10'\n" + 
    			"and c.resolutionCode ='1' ";
    	List<Map<String, Object>> firstL = ucmlS.selectList(firstSql);
    	int ct = firstL.size();
    	if(ct==0) {
    		hashtable.put("实施入口节点物料未更新状态", "无问题");
    	}else {
    		String codes="";
    		String paras="";
    		for(int i=0;i<ct;i++) {
    			Object object = firstL.get(i).get("Code");
    			if(i==ct-1) {
	    			codes += object.toString()+"|";
	    			paras += object.toString()+"','";
    			}else {
    				codes += object.toString()+"";
	    			paras += object.toString();
    			}
    		}
    		String upSql = " update AM_TMATUSELIST set CreateTime=GETDATE(),MatState='0'\n" + 
    				"where ExceptionReportID in ('"+paras+"') \n" + 
    				"and MatState\n" + 
    				"='6' ";
    		Integer update = ucmlS.update(upSql);
    		if(update>0) {
    			hashtable.put("实施入口节点物料未更新状态", "未处理的异常："+codes+",处理结果：全部处理");
    		}else {
    			hashtable.put("实施入口节点物料未更新状态", "未处理的异常："+codes+",处理结果：处理失败，请人工排查一下");
    		}
    	}
    	
    	//2、matuselist 和 material表关联丢失情况  很偶尔  暂未查到原因
    	String secondSql = " select count(a.am_tmatuselistoid) as nums from am_tmatuselist a\n" + 
    			"left join AM_TMATERIAL b on a.AM_TMATERIAL_FK = b.AM_TMATERIALOID\n" + 
    			"where a.CreateTime > '2019-1-1'\n" + 
    			"and b.AM_TMATERIALOID is null ";
    	List<Map<String, Object>> selectList = ucmlS.selectList(secondSql);
    	int size = selectList.size();
    	if(size > 0) {
    		Object object = selectList.get(0).get("nums");
    		int parseInt = Integer.parseInt(object.toString());// BUG数据的统计条数
    		if(parseInt > 0) {
    			hashtable.put("matuselist和 material表关联丢失情况", "存在丢失，请人工查看，并尝试定位问题");
    		}else {
    			hashtable.put("matuselist和 material表关联丢失情况", "无丢失错误发生");
    		}
    	}
    	
    	//3、物料订单做了 但是matstate状态位没更新一致    更新了相应程序后 目前此类问题已极少
    	String thirdSql = " SELECT b.ProcmOrderNo,b.WaitBuyQuantity,b.BuyQuantity,b.UseSpareQuantity,b.IsReceived,\n" + 
    			"b.Price \n" + 
    			"FROM AM_TMATUSELIST a\n" + 
    			"LEFT JOIN AM_TPROCMORDERSUB b ON a.AM_TMATUSELISTOID = b.AM_TMATUSELIST_FK\n" + 
    			"WHERE a.MatState='0' \n" + 
    			"AND isnull(b.Price,0)<>0\n" + 
    			"AND b.AM_TPROCMORDERSUBOID IS NOT NULL\n" + 
    			"and LinkType ='1' ";
    	List<Map<String, Object>> selectList2 = ucmlS.selectList(thirdSql);
    	int size2 = selectList2.size();
    	if(size2 > 0) {
    		String upString = " UPDATE AM_TMATUSELIST SET MatState = '1'\n" + 
    				"FROM AM_TMATUSELIST a\n" + 
    				"LEFT JOIN AM_TPROCMORDERSUB b ON a.AM_TMATUSELISTOID = b.AM_TMATUSELIST_FK\n" + 
    				"WHERE a.MatState='0' \n" + 
    				"AND isnull(b.Price,0)<>0\n" + 
    				"AND b.AM_TPROCMORDERSUBOID IS NOT NULL\n" + 
    				"and LinkType ='1' ";
    		Integer update = ucmlS.update(upString);
    		if(update>0) {
    			hashtable.put("matstate状态位没更新", "存在未更新，已自动识别并更新状态，处理数量："+update);
    		}else {
    			hashtable.put("matstate状态位没更新", "存在未更新，请人工查看，并尝试定位问题");
    		}
    		
    	}else {
    		hashtable.put("matstate状态位没更新", "无错误数据");
    	}
    	
    	//4、改善单多余的   自动清理多余垃圾的改善单（前一天的）
    	String fourSql = " delete\n" + 
    			"from entity_gs_info where entity_gs_infooid in (\n" + 
    			"select entity_gs_infooid from entity_gs_info\n" + 
    			"where GSUserName is null\n" + 
    			")\n" + 
    			"and  DATEDIFF(day,finishtime,getdate()) > 0 ";
    	Integer delete = ucmlS.delete(fourSql);
    	hashtable.put("改善单多余自动清理", "已完成清理任务，清理数："+delete);
    	
    	//5、询价单 未 启动流程清理 （前一天 不清理当天的数据）
    	String fifSql = " select a.FReqNo \n" + 
    			"from am_tconfirmp a \n" + 
    			"where a.SYS_Created > '2020-9-1'\n" + 
    			"and a.InstanceID is null and  DATEDIFF(day,a.SYS_Created,getdate()) > 0 ";
    	
    	List<Map<String, Object>> selectList3 = ucmlS.selectList(fifSql);
    	int size3 = selectList3.size();
    	if(size3 >0) {
    		String nos = "";
    		String pas = "";
    		for(int i=0;i<size3;i++) {
    			if(i==size3-1) {
    				nos += selectList3.get(i).get("FReqNo");
    				pas += selectList3.get(i).get("FReqNo");
    			}else {
    				nos += selectList3.get(i).get("FReqNo")+"|";
    				pas += selectList3.get(i).get("FReqNo")+"','";
    			}
    		}
    		String delM = " delete\n" + 
    				"from AM_TCONFIRMP\n" + 
    				"where FReqNo in ('"+pas+"') ";//删主表
    		String delS = " delete\n" + 
    				"from AM_TCONFIRMPSUB\n" + 
    				"where FReqNo in ('"+pas+"') ";//删子表
    		Integer delete2 = ucmlS.delete(delS);
    		Integer delete3 = ucmlS.delete(delM);
    		if(delete2+delete3 > 0) {
    			hashtable.put("询价单未启动流程清理", "已完成清理任务，清理数："+size3+";清理单号："+nos);
    		}else {
    			hashtable.put("询价单未启动流程清理", "执行清理任务失败，请人工排查，需清理的单号："+nos);
    		}
    	}else {
    		hashtable.put("询价单未启动流程清理", "当前任务无数据需清理");
    	}
    	
    	//6、--错误流程查询  不包含设变的
    	String sixSql = " SELECT count(a.taskticketoid) as nums FROM taskticket a \n" + 
    			"LEFT JOIN AssignTask b ON a.TaskTicketOID = b.TaskTicketOID\n" + 
    			"WHERE a.resolutionCode <> '1'\n" + 
    			"AND b.AssignTaskOID IS NULL\n" + 
    			"AND a.CreateTS > '2020-10-9'\n" + 
    			"and a.FlowID<>'FLow_11515VER10'\n" + 
    			"ORDER BY a.CreateTS ";
    	
    	List<Map<String, Object>> selectList4 = ucmlS.selectList(sixSql);
    	if(selectList4.size()>0) {
    		Object object = selectList4.get(0).get("nums");
    		int parseInt = Integer.parseInt(object.toString());
    		if(parseInt>0) {
    			hashtable.put("错误流程查询（不包含设变）","发现有异常数据，需人工辨别处理数："+parseInt);
    		}else {
    			hashtable.put("错误流程查询（不包含设变）","一切正常，无错误发生");
    		}
    	}
    	
    	//7、异常的错误流程查询 
    	String sevenSql = " SELECT count(c.AM_TERRORREPORTOID) as nums  FROM taskticket a \n" + 
    			"LEFT JOIN AssignTask b ON a.TaskTicketOID = b.TaskTicketOID\n" + 
    			"left join AM_TERRORREPORT c on c.InstanceID = a.InstanceID\n" + 
    			"WHERE a.resolutionCode <> '1'\n" + 
    			"AND b.AssignTaskOID IS NULL\n" + 
    			"AND a.CreateTS > '2020-10-9'\n" + 
    			"AND a.ActivityID <>\n" + 
    			"'AC_12442VER10'\n" + 
    			"and a.FlowID='FLow_11515VER10'\n" + 
    			"and (c.AM_TERRORREPORTOID is not null and c.Priority='0')\n" + 
    			"ORDER BY a.CreateTS ";
    	
    	List<Map<String, Object>> selectList5 = ucmlS.selectList(sevenSql);
    	if(selectList5.size() >0) {
    		Object object = selectList5.get(0).get("nums");
    		int parseInt = Integer.parseInt(object.toString());
    		if(parseInt>0) {
    			hashtable.put("异常的错误流程查询","发现有异常数据，需人工辨别处理数："+parseInt);
    		}else {
    			hashtable.put("异常的错误流程查询","一切正常，无错误发生");
    		}
    	}
    	
    	//8、采购单任务无下一级
    	String eightSql = " select a.procmorderno from AM_TPROCMORDER a \n" + 
    			"left join AssignTask c on c.InstanceID = a.InstanceID and c.resolutioncode ='0'\n" + 
    			"where a.DeleteFlag='0'\n" + 
    			"and a.OrderState in ('0','1') \n" + 
    			"and c.AssignTaskOID is null\n" + 
    			"and a.CreateTime > '2020-7-1' ";
    	
    	List<Map<String, Object>> selectList6 = ucmlS.selectList(eightSql);
    	int size4 = selectList6.size();
    	if(size4 > 0) {
    		String noS = "";
    		for(int i=0;i<size4;i++) {
    			noS += selectList6.get(i).get("procmorderno").toString()+"|";
    		}
    		hashtable.put("采购单任务无下一级","发现错误，错误数："+size4+"订单号："+noS);
    	}else {
    		hashtable.put("采购单任务无下一级","一切正常，无错误发生");
    	}
    	
    	//9、--明细表新制导入无待办人错误   只有郑强胜的电脑有此问题，现在转岗  后续未知是否会有类似情况 继续跟踪一段时间
    	String ninSql = " select count(a.assigntaskoid) as nums from assigntask a\n" + 
    			" left join TaskTicket b on a.TaskTicketOID = b.TaskTicketOID\n" + 
    			" where a.resolutioncode ='0' and isnull(a.USR_LOGIN,'') = ''\n" + 
    			" and a.SYS_Created > '2020-7-1'\n" + 
    			" and b.FlowID='FLow_10806VER10' ";
    	List<Map<String, Object>> selectList7 = ucmlS.selectList(ninSql);
    	if(selectList7.size() > 0) {
    		String string = selectList7.get(0).get("nums").toString();  
    		int parseInt = Integer.parseInt(string);
    		if(parseInt > 0) {
    			hashtable.put("明细表新制导入无待办人错误","发现错误，错误数："+string+",目前暂不做自动处理，人工先查看");
    		}else {
    			hashtable.put("明细表新制导入无待办人错误","一切正常，无错误发生");
    		}
    	}
    	
    	//10、多余模具 无外键信息的数据
    	String tenSql = " delete\n" + 
    			"from am_tmold \n" + 
    			"where aM_tmake_fk not in (\n" + 
    			"select AM_TMAKEOID from AM_TMAKE\n" + 
    			")\n" + 
    			"or aM_tmake_fk is null ";
    	
    	Integer delete2 = ucmlS.delete(tenSql);
    	if(delete2 > 0) {
    		hashtable.put("多余模具清理", "已完成清理任务，清理数："+delete2);
		}else {
			hashtable.put("多余模具清理", "无错误数据需处理");
		}
    	
    	//11、订单流程未发起的数据查询
    	String eleSql = " select procmorderno from AM_TPROCMORDER \n" + 
    			"where CreateTime > '2020-7-1'\n" + 
    			"and ProcmType <> '2'\n" + 
    			"and DeleteFlag = '0'\n" + 
    			"and InstanceID is null\n" + 
    			"and isnull(OrderState,'')  in ('','0','1') ";
    	
    	List<Map<String, Object>> selectList8 = ucmlS.selectList(eleSql);
    	int size5 = selectList8.size();
    	if(size5 > 0) {
    		String noS = "";
    		for(int i=0;i<size5;i++) {
    			Object object = selectList8.get(i).get("procmorderno");
    			noS += object.toString() + "|";
    		}
    		hashtable.put("订单流程未发起的数据查询", "错误数据条数:"+size5+";对应订单号："+noS);
    	}else {
    		hashtable.put("订单流程未发起的数据查询", "无错误数据需处理");
    	}
    	
    	//12、另采订单查询物料外键丢失的bug
    	String tweSql = " select distinct a.ProcmOrderNo\n" + 
    			"from AM_TPROCMORDERSUB a\n" + 
    			"left join AM_TREQUESTMATSUB b on a.AM_TREQUESTMATSUB_FK = b.AM_TREQUESTMATSUBOID\n" + 
    			" join am_tprocmorder c on c.am_tprocmorderoid = a.am_tprocmorder_fk and c.CreateTime > '2020-7-1'\n" + 
    			"where AM_TREQUESTMATSUB_FK IS NOT NULL\n" + 
    			"and b.AM_TMATUSELIST_FK <> a.AM_TMATUSELIST_FK ";
    	
    	List<Map<String, Object>> selectList9 = ucmlS.selectList(tweSql);
    	int size6 = selectList9.size();
    	if(size6 > 0) {
    		String noS = "";
    		for(int i=0;i<size6;i++) {
    			Object object = selectList9.get(i).get("procmorderno");
    			noS += object.toString() + "|";
    		}
    		hashtable.put("另采订单查询物料外键丢失", "错误数据条数:"+size6+";对应订单号："+noS);
    	}else {
    		hashtable.put("另采订单查询物料外键丢失", "无错误数据需处理");
    	}
    	
    	//13、设计管理  数据记录有问题的
    	String a3Sql = " update entity_fs_worklog set ProName=d.ProjectCode,make=c.CPJTH,gxh=b.gxh\n" + 
    			"from entity_fs_worklog a\n" + 
    			"left join am_tmold b on a.am_tmold_fk = b.am_tmoldoid \n" + 
    			"left join am_tmake c on c.am_tmakeoid = b.am_tmake_fk\n" + 
    			"left join am_tproject2 d on d.AM_TPROJECT2OID = c.AM_TPROJECT2_FK\n" + 
    			"where a.AM_TMOLD_FK is not null and a.gxh <> 'No Value'\n" + 
    			"and (d.ProjectCode <> a.ProName or c.CPJTH <> a.make or b.gxh <> a.gxh ) ";
    	
    	Integer update = ucmlS.update(a3Sql);
    	if(update >0) {
    		hashtable.put("设计管理日志数据记录不对应", "已完成清理任务，完成条数："+update);
    	}else {
    		hashtable.put("设计管理日志数据记录不对应", "无错误数据需处理");
    	}
    	
    	//14、订单收货后状态不对
    	
    	String a4Sql = " select * from (\n" + 
    			"select \n" + 
    			"case when n1 = 0 then 3\n" + 
    			"when n1>0 and n1 < n2 then 2\n" + 
    			"when n1>0 and n1 = n2 then 4 end as DD,n1,n2,OrderState,ProcmOrderNo\n" + 
    			" from (\n" + 
    			"select (select count(*)  from AM_TPROCMORDERSUB where AM_TPROCMORDERSUB.ProcmOrderNo = AM_TPROCMORDER.ProcmOrderNo and IsReceived in ('0','1') ) as n1\n" + 
    			",(select count(*)  from AM_TPROCMORDERSUB where AM_TPROCMORDERSUB.ProcmOrderNo = AM_TPROCMORDER.ProcmOrderNo ) as n2\n" + 
    			",OrderState,AM_TPROCMORDER.ProcmOrderNo\n" + 
    			"from AM_TPROCMORDER \n" + 
    			"left join AM_TPROCMORDERSUB b on b.AM_TPROCMORDER_FK = AM_TPROCMORDER.AM_TPROCMORDEROID\n" + 
    			"where OrderDate > '2020-1-1' and OrderState <> '3'\n" + 
    			"and DeleteFlag = '0' and b.AM_TPROCMORDERSUBOID is not null \n" + 
    			"group by AM_TPROCMORDER.ProcmOrderNo,OrderState\n" + 
    			")t\n" + 
    			")tt where dd <> OrderState and OrderState <>'1' and OrderState <>'0' and dd = '3' ";
    	
    	List<Map<String, Object>> selectList10 = ucmlS.selectList(a4Sql);
    	int size7 = selectList10.size();
    	if(size7 >0) {
    		String noS = "";
    		for(int i=0;i<size7;i++) {
    			Object object = selectList10.get(i).get("ProcmOrderNo");
    			noS += object.toString() + "|";
    		}
    		hashtable.put("订单收货后状态不对", "错误数据条数:"+size7+";对应订单号："+noS);
    	}else {
    		hashtable.put("订单收货后状态不对", "无错误数据需处理");
    	}
    	
    	//15、备品库使用逻辑更正
    	String a5Sql = " update AM_TMaterialSpare set CanUseQuantity = t.StoreQuant-t.procmNums\n" + 
    			"from AM_TMaterialSpare b  \n" + 
    			"left join (\n" + 
    			"select \n" + 
    			"(\n" + 
    			"select isnull(sum(UseSpareQuantity - VoucherSpareQuant),0) from AM_TPROCMORDERSUB where AM_TPROCMORDERSUBOID in (\n" + 
    			"select AM_TPROCMORDERSUB_FK from AM_TMATSPARESUB where AM_TMaterialSpare_FK = AM_TMaterialSpare.AM_TMaterialSpareOID\n" + 
    			")\n" + 
    			") as procmNums,CanUseQuantity,StoreQuant,StoreQuant - CanUseQuantity as NowQ,AM_TMaterialSpareoid,wholespeccode\n" + 
    			"from AM_TMaterialSpare \n" + 
    			"where StoreQuant > 0 --CanUseQuantity < 0  \n" + 
    			")t on b.AM_TMaterialSpareoid = t.AM_TMaterialSpareoid\n" + 
    			"where procmNums <>  NowQ ";
    	
    	Integer update2 = ucmlS.update(a5Sql);
    	if(update2 > 0) {
    		hashtable.put("备品库使用逻辑更正", "自动处理逻辑更新，处理数："+update2);
    	}else {
    		hashtable.put("备品库使用逻辑更正", "无错误数据需处理");
    	}
    	
    	return hashtable;
    } 
    
    public static String getMemery() {

        OperatingSystemMXBean osmxb = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long totalvirtualMemory = osmxb.getTotalSwapSpaceSize(); // 剩余的物理内存
        long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();
        Double compare = (Double) (1 - freePhysicalMemorySize * 1.0 / totalvirtualMemory) * 100;
        String str = compare.intValue() + "%";
        return str;

    }
}

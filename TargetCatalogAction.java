package dms.lite.web.action.catalog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dms.lite.core.entities.ConfigValueMapping;
import dms.lite.core.entities.Kpi;
import dms.lite.core.entities.KpiPlan;
import dms.lite.core.entities.Staff;
import dms.lite.core.entities.StaffGroup;
import dms.lite.core.entities.enumtype.ActiveType;
import dms.lite.core.entities.enumtype.KpiPlanType;
import dms.lite.core.entities.enumtype.StaffType;
import dms.lite.core.entities.filter.TargetFilter;
import dms.lite.core.entities.filter.RountingFilter;
import dms.lite.core.entities.filter.StaffFilter;
import dms.lite.core.entities.vo.KpiVO;
import dms.lite.core.entities.vo.ObjectVO;
import dms.lite.web.action.general.AbstractAction;
import dms.lite.web.enumtype.VSARole;
import dms.lite.web.utils.LogUtility;
import dms.lite.web.utils.ReportUtils;

public class TargetCatalogAction extends AbstractAction{
	//** The Constant serialVersionUID. *//*
	private static final long serialVersionUID = 4895182292899957718L;
	
	//** The staff sign. *//*
	private Staff staffSign;
	
	//** The excel file. *//*
	private File excelFile;
	
	private String excelFileContentType;
	
	//** The lst view. *//*
//	private List<CellBean> lstView;
	
	//** The kpi plan object vo. *//*
//	private KpiPlanObjectVO kpiPlanObjectVO;

	//** The title. *//*
	private String title;
	
	//** The target type. *//*
	private Integer kpiType;
	
//	private List<Kpi> lst;
	
	private Kpi kpi;
	
	private String monthPlan;
	
	private Integer totalRow;
		
	private List<String> lstObjectCode;
	
	private ReportUtils rUtils;

	private String successMsg;

	private String errMsg;

	private String routingCode;
	
	private String staffCode;
	
	private String codeName;

	private Boolean isSupervisor = false;
	private Long loginStaffId;

	@Override
	public void prepare(){		
		try {
			super.prepare();
			staffSign = getStaffByCurrentUser();
			rUtils = new ReportUtils();
			
		} catch (Exception e) {
			LogUtility.logError(e, e.getMessage());
		}
	}
	

	@Override
	public String execute() {
		generateToken();
//		if(isValidDmsVer == false){
//			return PAGE_NOT_FOUND;
//		}
		try {
			getChooseGroup();
			if(staffSign==null){
				return PAGE_NOT_PERMISSION;
			}
			ConfigValueMapping valueMapping = configMgr.getConfigValueMappingByShopAndConfigCode(staffSign.getId(), "CF_AMOUNT_CAL_BY_OBJECT");
			if(valueMapping==null){
				return PAGE_NOT_FOUND; 
			}		
//			if(KpiPlanType.STAFF.getValue().toString().equals(valueMapping.getValue())){
//				kpiType = KpiPlanType.STAFF.getValue();
//				title = Configuration.getResourceString(ConstantManager.VI_LANGUAGE,"kpi.title.staff");
//			}
			
			loginStaffId = staffSign.getId();
			
			if( checkVSARoles(VSARole.DMS_LITE_SUPERVISE)){
				isSupervisor = true;
			}else{
				isSupervisor = false;
			}
			
			return SUCCESS;
		} catch (Exception e) {
			LogUtility.logError(e, e.getMessage());
		}
		return SUCCESS;
	}
	
	public String initData(){
		System.out.println("get data");
		try {
			if(staffSign==null){
				return JSON;
			}
			
			//** Danh cac loai KPI danh cho don vi *//
			TargetFilter filter = new TargetFilter();
			filter.setActiveType(ActiveType.RUNNING);	
	
	//		filter.setMonthPlan(DateUtil.parse(monthPlan, DateUtil.DATE_M_Y));
			filter.setMonthPlan("01/"+monthPlan);

			
			
			StaffFilter staffFilter = new StaffFilter();
//			RountingFilter rFilter = new RountingFilter();
			
			StaffGroup staffGroup = getChooseGroup();
			
			if(staffGroup!=null){ // role gs
				filter.setGroupId(staffGroup.getId());
				staffFilter.setStaffGroupId(staffGroup.getId());
//				rFilter.setStaffGroupId(staffGroup.getId());				
			}			
			if (staffSign != null && staffSign.getStaffType().getType().getValue().equals(StaffType.SUPERVISOR.getValue())){
				filter.setStaffManagerId(staffSign.getId());
			}
			
			staffFilter.setStaffObjectType(staffSign.getStaffType().getType().getValue());
//			rFilter.setShopId(staffSign.getShop().getId());
//			rFilter.setStatus(ActiveType.RUNNING);
//			rFilter.setStaffObjectType(staffSign.getStaffType().getObjectType());
//			if(KpiPlanType.STAFF.getValue().equals(kpiType)){			
//				staffFilter.setListStaffType(Arrays.asList(StaffType.BA)); 
//				staffFilter.setCodeName(codeName);
//				staffFilter.setStaffId(staffSign.getId());
//				ObjectVO<Staff>  objectVO = staffMgr.getListStaffByFilter(staffFilter);
//				List<Staff> listStaff = objectVO.getLstObject();
//				result.put("listStaff", listStaff);
//			}
//			List<KpiVO> listKpi = targetMgr.getLisKpiVO(filter);
//			if(listKpi==null){
//				listKpi = new ArrayList<KpiVO>();				
//			}			
//			filter.setTargetType(kpiType);	
			if(checkVSARoles(VSARole.DMS_LITE_SUPERVISE)){
				filter.setSupervisorId(staffSign.getId());
			}
//			List<KpiPlan> listKpiPlan = kpiMgr.getListKpiPlanByFilter(filter);
//			if(listKpiPlan==null){
//				listKpiPlan = new ArrayList<KpiPlan>();				
//			}			
//			result.put("listKpi", listKpi);
//			result.put("listKpiPlan", listKpiPlan);
		} catch (Exception e) {
			LogUtility.logError(e, e.getMessage());
		}
		return JSON;
	}
	
//	public String exportKpiRateTemplate(){
//		try{
//			AbstractAction.isForExport = true;
//			request = ServletActionContext.getRequest();
//			Long shopId = (Long) request.getSession().getAttribute(ConstantManager.SESSION_SHOP_ID);
//			
//			List<List<CellBean>> listFill = new ArrayList<List<CellBean>>();
//			List<CellBean> lstFails = new ArrayList<CellBean>();
//			listFill.add(lstFails);
//			
//			String headerText = "";
//			
//			ConfigValueMapping cfUseOrderModule = configMgr.getConfigValueMappingByShopAndConfigCode(shopId, ConfigType.CF_AMOUNT_CAL_BY_OBJECT.getValue());
//			if (cfUseOrderModule != null){
//				//Staff staff = staffMgr.getStaffByShopAndStaffCode(shopId, this.currentUser.getUserName());
//				Staff staff = staffMgr.getStaffByStaffLoginCode(this.currentUser.getUserName());
//				StaffGroup chooseGroup = getChooseGroup();
//				if (staff == null){
//					return JSON;
//				}
//				List<CellBean> lstObject = new ArrayList<CellBean>();
//				if (Integer.parseInt(cfUseOrderModule.getValue()) == 1){	// NVBH
//					headerText = Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.rate.template.file.header.staff");
//					StaffFilter staffFilter = new StaffFilter();
//					staffFilter.setShopId(shopId);
//					staffFilter.setListStaffType(Arrays.asList(StaffType.NVBH,StaffType.NVBH_VANSALE));
//					staffFilter.setStatus(ActiveType.RUNNING);
//					ObjectVO<Staff> objectVO = null;
//					if (StaffType.ADMIN.getValue().equals(staff.getStaffType().getObjectType())){
//						
//					} else if (StaffType.GS.getValue().equals(staff.getStaffType().getObjectType())){
//						staffFilter.setStaffOwnerId(staff.getId());
//						staffFilter.setLstStaffGroupId(chooseGroup.getId().toString());	
//						staffFilter.setStaffGroupId(chooseGroup.getId());
//					} else if (StaffType.QL.getValue().equals(staff.getStaffType().getObjectType())){
//						staffFilter.setStaffObjectType(staff.getStaffType().getObjectType());
//						staffFilter.setStaffId(staff.getId());
//					}
//					objectVO = staffMgr.getListStaffByFilter(staffFilter);
//					if (objectVO != null && objectVO.getLstObject() != null){
//						List<Staff> lstSaler = objectVO.getLstObject();
//						for (Staff s : lstSaler){
//							CellBean cellBean = new CellBean();
//							cellBean.setContent1(s.getStaffCode());
//							cellBean.setContent2(s.getStaffName());
//							cellBean.setContent3(s.getStaffCode() + "-" + s.getStaffName());
//							lstObject.add(cellBean);							
//						}
//					}
//				} else if (Integer.parseInt(cfUseOrderModule.getValue()) == 2){	// TUYEN
//					headerText = Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.rate.template.file.header.routing");
//					RountingFilter filter = new RountingFilter();
//					filter.setShopId(shopId);
//					filter.setIsGetChild(false);
//					filter.setStatus(ActiveType.RUNNING);
//					
//					*//**
//					 * get supervisor's routing
//					 * @author tuannd20
//					 * @date 02/07/2014
//					 *//*
////					/Staff loginStaff = staffMgr.getStaffByShopAndStaffCode(shopId, this.currentUser.getUserName());
//					Staff loginStaff = staffMgr.getStaffByStaffLoginCode(this.currentUser.getUserName());
//					if (StaffType.GS.getValue().equals(loginStaff.getStaffType().getObjectType())){
//						filter.setSupervisorId(loginStaff.getId());
//						filter.setStaffGroupId(chooseGroup.getId());
//					}					
//					*//**END*//*
//					*//**
//					 * @author lochp
//					 * 
//					 *//*
//					filter.setStaffObjectType(staff.getStaffType().getObjectType());
//					if (StaffType.QL.getValue().equals(loginStaff.getStaffType().getObjectType())){
//						filter.setStaffId(staff.getId());
//					}
//					List<Routing> lstRouting = superviserMgr.getListRoutingByShop(filter, null);
//					if (lstRouting != null){
//						for (Routing r : lstRouting){
//							CellBean cellBean = new CellBean();
//							cellBean.setContent1(r.getRoutingCode());
//							cellBean.setContent2(r.getRoutingName());
//							cellBean.setContent3(r.getRoutingCode() + "-" + r.getRoutingName());
//							lstObject.add(cellBean);
//						}
//					}
//				}
//				listFill.add(lstObject);
//			}
//			
//			KpiFilter filter = new KpiFilter();
//			filter.setShopId(shopId);
//			filter.setActiveType(ActiveType.RUNNING);
//			filter.setMonthPlan(DateUtil.toDateString(DateUtil.getSysdateFromDatabase(), "dd/MM/yyyy"));
////			filter.setImportFlag(true);
//			List<Kpi> lstKpi = kpiMgr.getLisKpiByFilter(filter);
//			List<CellBean> lstColumn = new ArrayList<CellBean>();
//			if (lstKpi != null){
//				for (Kpi kpi : lstKpi){
//					CellBean cellBean = new CellBean();
//					cellBean.setContent1(kpi.getKpiName());
//					if(!StringUtil.isNullOrEmpty(mulLanguage) && (mulLanguage.equals("vi") || mulLanguage.equals("vn") )){
//						cellBean.setContent1(kpi.getKpiNameVi());
//					}else {
//						cellBean.setContent1(kpi.getKpiNameEn());
//					}
//					lstColumn.add(cellBean);
//				}
//			}
//			listFill.add(lstColumn);
//			
//			
//			List<List<CellBean>> lstData = listFill;
//			String tempFileName = ConstantManager.TEMPLATE_KPI_RATE_EXPORT_EN;
//			if(mulLanguage != null && (mulLanguage.equalsIgnoreCase("vn") || mulLanguage.equalsIgnoreCase("vi"))){
//				tempFileName = ConstantManager.TEMPLATE_KPI_RATE_EXPORT;
//			}
//			try {
//				//Begin anhdt10 -fix bug attt-  them usser Id vao ten file
//				Long  userId = currentUser==null?null:currentUser.getUserID();
//				String strUserId = userId==null? "UID" :userId.toString();
//				//End anhdt10
//				if(lstData != null && lstData.size() > 0){
//					String templateFileName = ServletActionContext.getServletContext().getRealPath("/") + Configuration.getExcelTemplatePathKpi() + tempFileName;
//					templateFileName = templateFileName.replace('/', File.separatorChar);
//					String outputName = DateUtil.toDateString(DateUtil.now(), DateUtil.DATE_FORMAT_EXCEL_FILE) + "_" +  tempFileName;
//					//Begin anhdt10 - fix bug attt - them usser Id vao ten file
//					String exportFileName = Configuration.getStoreRealPath() + strUserId + "_"+  outputName;
//					//End anhdt10
////					String exportFileName = Configuration.getStoreRealPath() + outputName;
//					Map<String, Object> params = new HashMap<String, Object>();
//					params.put("report", lstData.get(0));
//					params.put("lstObject", lstData.get(1));
//					params.put("lstColumn", lstData.get(2));
//					params.put("headerColumnName", headerText);
//					params.put("month", Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "jsp.khtt.export.import.temp.thang.nam"));
//					InputStream inputStream = new BufferedInputStream(new FileInputStream(templateFileName));
//					XLSTransformer transformer = new XLSTransformer();
//					org.apache.poi.ss.usermodel.Workbook resultWorkbook = transformer.transformXLS(inputStream, params);
//					inputStream.close();
//					OutputStream os = new BufferedOutputStream(new FileOutputStream(exportFileName));
//					resultWorkbook.write(os);
//					os.flush();
//					os.close();
////					String outputPath = Configuration.getExportExcelPath() + outputName;
//					//Begin anhdt10 fix attt
//					String outputPath = outputName;
//					//End anhdt10
//					result.put(LIST, outputPath);
//				}else{
//					result.put("hasData", false);
//				}
//			} catch (ParsePropertyException e) {
//				LogUtility.logErrorStandard(e, e.getMessage(), logInfo);
//			} catch (InvalidFormatException e) {
//				LogUtility.logErrorStandard(e, e.getMessage(), logInfo);
//			} catch (IOException e) {
//				LogUtility.logErrorStandard(e, e.getMessage(), logInfo);
//			}
//			AbstractAction.isForExport = false;
//	    }catch (Exception e) { 
//	    	logInfo.setFuncType(FuncType.READ);
//	    	result.put(ERROR, true);
//	    	LogUtility.logErrorStandard(e, e.getMessage(), logInfo);
//	    }
//	    return JSON;
//	}
	
//	public String saveData(){
//		try {
//			
//			resetToken(result);		
//			
//			result.put(ERROR, true);
//			if(staffSign==null || staffSign.getShop()==null){
//				return JSON;
//			}
//			Shop shop = staffSign.getShop();
//			kpiPlanObjectVO.setShop(shop);			
//			kpiMgr.saveDataKpiPlan(kpiPlanObjectVO);
//			result.put(ERROR, false);
//			result.put("successMsg",Configuration.getResourceString("vi","kpi.save.data.success")  );
//		} catch (Exception e) {
//			result.put("errMsg", ValidateUtil.getErrorMsg(ConstantManager.ERR_SYSTEM));
//			LogUtility.logError(e, "KpiCatalogAction.saveData : " + e.getMessage());
//		}
//		return JSON;
//	}
	
//public String exportKpi(){		
//		
//		try{
//			AbstractAction.isForExport = true;			
//			if(staffSign==null || staffSign.getShop()==null){
//				return JSON;
//			}
//			
//			List<Staff> listStaff = new ArrayList<Staff>();
//			List<Routing> listRouting = new ArrayList<Routing>();
//			
//			*//** Danh cac loai KPI danh cho don vi *//*
//			KpiFilter filter = new KpiFilter();
//			filter.setActiveType(ActiveType.RUNNING);
//			filter.setShopId(staffSign.getShop().getId());	
//			
//			
//		//	filter.setMonthPlan(DateUtil.parse(monthPlan, DateUtil.DATE_M_Y));			
//			filter.setMonthPlan("01/"+monthPlan);
//			
//			
//			StaffFilter staffFilter = new StaffFilter();
//			RountingFilter rFilter = new RountingFilter();
//			
//			StaffGroup staffGroup = getChooseGroup();
//			
//			if(staffGroup!=null){
//				filter.setGroupId(staffGroup.getId());
//				staffFilter.setStaffGroupId(staffGroup.getId());
//				rFilter.setStaffGroupId(staffGroup.getId());				
//			}			
//			staffFilter.setShopId(staffSign.getShop().getId());
//			rFilter.setShopId(staffSign.getShop().getId());
//			rFilter.setStatus(ActiveType.RUNNING);
//			
//
//			
//			if(KpiPlanType.STAFF.getValue().equals(kpiType)){
//				staffFilter.setListStaffType(Arrays.asList(StaffType.NVBH,StaffType.NVBH_VANSALE));
//				staffFilter.setCodeName(codeName);
//				ObjectVO<Staff>  objectVO = staffMgr.getListStaffByFilter(staffFilter);
//				listStaff = objectVO.getLstObject();
//				result.put("listStaff", listStaff);
//			}else{				
//				
//				//Staff loginStaff = staffMgr.getStaffByShopAndStaffCode(staffSign.getShop().getId(), this.currentUser.getUserName()); 
//				Staff loginStaff = staffMgr.getStaffByStaffLoginCode(this.currentUser.getUserName());
//				
//				
//				if (StaffType.GS.getValue().equals(loginStaff.getStaffType().getObjectType())){
//					rFilter.setSupervisorId(loginStaff.getId());
//				}	
//				rFilter.setCodeName(codeName);
//
//				listRouting = superviserMgr.getListRoutingByShop(rFilter,null);
//				if(listRouting==null){
//					listRouting = new ArrayList<Routing>();
//				}
//				result.put("listRouting", listRouting);
//			}
//			
//			List<Kpi> listKpi = kpiMgr.getLisKpiByFilter(filter);
//			if(listKpi==null){
//				listKpi = new ArrayList<Kpi>();				
//			}			
//			filter.setKpiType(kpiType);			
//			List<KpiPlan> listKpiPlan = kpiMgr.getListKpiPlanByFilter(filter);
//			if(listKpiPlan==null){
//				listKpiPlan = new ArrayList<KpiPlan>();				
//			}
//	
//			
//			if(listKpiPlan != null && listKpiPlan.size()>0){
//				if(KpiPlanType.STAFF.getValue().equals(kpiType)){
//					exportStaffKip(listStaff,listKpi,listKpiPlan);
//				}else{
//					if(isValidDmsVer == true){
//						exportRoutingKpi(listRouting, listKpi, listKpiPlan);
//					}else{
//						result.put("hasData", false);
//						result.put(ERROR, true);
//						return JSON;
//					}
//				}
//			}else{
//				result.put("hasData", false);
//				return JSON;
//			}
//			AbstractAction.isForExport = false;
//		} catch (Exception ex) {
//			LogUtility.logError(ex, "CustomerCatalogAction.exportCustomer - " + ex.getMessage());
//			result.put(ERROR, true);
//			result.put("errMsg", ValidateUtil.getErrorMsg(ConstantManager.ERR_SYSTEM));
//		}
//
//		return JSON;
//		
//	}
	
//	public String exportStaffKip(List<Staff>lstStaff, List<Kpi>lstKpi, List<KpiPlan>lstKpiPlan){
//		FileOutputStream out = null;
//		SXSSFWorkbook workbook = null;
//		AbstractAction.isForExport = true;
//		try{
//			//Begin anhdt10 -fix bug attt-  them usser Id vao ten file
//			Long  userId = currentUser==null?null:currentUser.getUserID();
//			String strUserId = userId==null? "UID" :userId.toString();
//			//End anhdt10
//			//Init XSSF workboook
//			String name = ConstantManager.EXPORT_KPI_STAFF_EN;
//			if(!StringUtil.isNullOrEmpty(mulLanguage) && (mulLanguage.equals("vn") || mulLanguage.equals("vi"))){
//				name = ConstantManager.EXPORT_KPI_STAFF;
//			}
//			String outputName = DateUtil.toDateString(DateUtil.now(), DateUtil.DATE_FORMAT_EXCEL_FILE) + "_" + name;
////			String exportFileName = Configuration.getStoreRealPath() + outputName;
//			//Begin anhdt10 - fix bug attt - them usser Id vao ten file
//			String exportFileName = Configuration.getStoreRealPath() + strUserId + "_"+  outputName;
//			//End anhdt10
//			
//			workbook = new SXSSFWorkbook(-1);
//			String sheetName = Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "report.staff.kpi");
//		    SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(sheetName);
//		    Map<String, XSSFCellStyle> styles = ExcelProcessUtils.createStylesPOI(workbook);
//
//		    XSSFColor darkBlue40 = new XSSFColor(new java.awt.Color(83, 142, 213));
//			XSSFFont gridTitleFont = (XSSFFont)workbook.createFont();
//			gridTitleFont.setFontHeightInPoints((short)9);
//			gridTitleFont.setFontName("Arial");
//			gridTitleFont.setBold(true);
//			gridTitleFont.setColor(IndexedColors.WHITE.getIndex());
//			XSSFCellStyle gridTitleFormat = (XSSFCellStyle)workbook.createCellStyle();
//			gridTitleFormat.setFont(gridTitleFont);
//			gridTitleFormat.setAlignment(XSSFCellStyle.ALIGN_CENTER);
//			gridTitleFormat.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
//			gridTitleFormat.setFillForegroundColor(darkBlue40);
//			gridTitleFormat.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
//			gridTitleFormat.setBorderTop(BorderStyle.THIN);
//			gridTitleFormat.setTopBorderColor(IndexedColors.BLACK.getIndex());
//			gridTitleFormat.setBorderBottom(BorderStyle.THIN);
//			gridTitleFormat.setBottomBorderColor(IndexedColors.BLACK.getIndex());
//			gridTitleFormat.setBorderRight(BorderStyle.THIN);
//			gridTitleFormat.setRightBorderColor(IndexedColors.BLACK.getIndex());
//			gridTitleFormat.setWrapText(true);
//
//			XSSFColor lightBlue = new XSSFColor(new java.awt.Color(0, 176, 240));
//			XSSFCellStyle gridTitleFormat2 = (XSSFCellStyle)gridTitleFormat.clone();
//			gridTitleFormat2.setFillForegroundColor(lightBlue);
//
//			XSSFColor red = new XSSFColor(new java.awt.Color(218, 150, 148));
//			XSSFCellStyle gridTitleFormatRed = (XSSFCellStyle)gridTitleFormat.clone();
//			gridTitleFormatRed.setFillForegroundColor(red);
//			
//		    sheet.setDisplayGridlines(false);
//		    sheet.setDisplayZeros(false);
//		    //end init XSSF
//		    
//		    //Style
//	    	XSSFCellStyle curFormatLeft = styles.get(ExcelProcessUtils.DETAIL_NORMAL_DOTTED_LEFT);
//	    	XSSFCellStyle curFormatRight = styles.get(ExcelProcessUtils.DETAIL_NORMAL_DOTTED_RIGHT);
//	    	XSSFCellStyle curFormatCenter= styles.get(ExcelProcessUtils.DETAIL_NORMAL_DOTTED_CENTER);
//	    	
//		    //set Row Height - Column width
//		    rUtils.setRowsHeight(sheet, 0, 30,15);
//		    rUtils.setColumnsWidth(sheet, 0, 60, 150, 150, 150);
//		    
//		    
//		    //static menu
//		    String[] menu = Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "report.staff.kpi.menu").split(",");
//		    for(int i=0; i<menu.length;i++){
//		    	rUtils.addCell(sheet, i, 0, menu[i], gridTitleFormat);
//		    }
//		    
//		    
//		    Kpi tmpKpi;
//		    //dynamic menu
//		    int colIndex = 4, rowIndex = 0;
//		    for(int i=0; i< lstKpi.size(); i++){
//		    	tmpKpi = lstKpi.get(i);
//		    	rUtils.setColumnWidth(sheet, colIndex, 150);
//		    	if(!StringUtil.isNullOrEmpty(mulLanguage) && (mulLanguage.equals("vn") || mulLanguage.equals("vi"))){
//		    		rUtils.addCell(sheet, colIndex, rowIndex, tmpKpi.getKpiNameVi(), gridTitleFormat);
//				}else{
//					rUtils.addCell(sheet, colIndex, rowIndex, tmpKpi.getKpiNameEn(), gridTitleFormat);
//				}
//		    	colIndex ++;
//		    }
//		    		    
//		    //data
//		    //tao map data kpi plan
//		    KpiPlan tmpKpiPlan;
//		    HashMap<String, KpiPlan>lstKpiPlanValue = new HashMap<String, KpiPlan>();
//		    for(int i=0; i<lstKpiPlan.size(); i++){
//		    	tmpKpiPlan = lstKpiPlan.get(i);
//		    	lstKpiPlanValue.put(tmpKpiPlan.getStaff().getId()+"_"+tmpKpiPlan.getKpi().getId(), tmpKpiPlan);
//		    }
//		    
//		    Long tmpkpiId, tmStaffId;
//		    Kpi tmKpi;		    
//		    
//		    for(int j = 0 ; j < lstStaff.size() ; j++){
//		    	Staff tmStaff = lstStaff.get(j);
//		    	tmStaffId = tmStaff.getId();
//		    	int nullValue = 0;
//		    	  	
//		    	
//		    	for (int k = 0 ; k < lstKpi.size();k++){
//		    		KpiFilter filter = new KpiFilter();			    	
//		    		// set kpi_id
//		    		tmKpi = lstKpi.get(k);
//		    		tmpkpiId = tmKpi.getId();		    		
//		    		filter.setKpiId(tmpkpiId);	
//		    		
//		    		// set kpi Type
//		    		filter.setKpiType(kpiType);
//		    		
//		    		// set shop_id
//		    		Shop shop = getCurrentShop();
//		    		filter.setShopId(shop.getId());		    		
//		    		// --- set staff_id --- 
//		    		filter.setStaffId(tmStaffId);		
//		    			    		
//		    		
//		    		List<KpiPlan> lst = kpiMgr.getListKpiPlanByFilter(filter);
//		    		if(lst==null || lst.size()==0){
//		    			nullValue ++;
//		    		}
//		    	}
//		    	
//		    	if(nullValue == lstKpi.size()){
//		    		lstStaff.remove(j);
//		    	}
//		    	
//		    	
//		    }
//		    
//		    
//		    Staff tmpStaff;
//		    colIndex = 4;
//		    rowIndex = 1;
//		    Long staffId, KpiId;
//		    
//		    for(int i=0; i<lstStaff.size(); i++){
//		    	tmpStaff = lstStaff.get(i);
//		    	if(tmpStaff != null){		    
//    	    		staffId = tmpStaff.getId();
//	    			colIndex = 4;
////	    			boolean isSuccess= false;
//	    			int countExitsKPI = 0;
//	    			for(int j=0; j<lstKpi.size(); j++){
//		    			tmpKpi = lstKpi.get(j);
//		    			if(tmpKpi != null){
//		    				KpiId = tmpKpi.getId();
//		    				if(lstKpiPlanValue.get(staffId+"_"+KpiId) != null){
//		    					countExitsKPI++;
//		    					break;
//		    				}
//		    			}
//	    			}
//	    			if(countExitsKPI == 0){
//	    				continue;
//	    			}
//		    		for(int j=0; j<lstKpi.size(); j++){
//		    			
//		    			tmpKpi = lstKpi.get(j);
//		    			if(tmpKpi != null){
//		    				KpiId = tmpKpi.getId();
//		    				
//		    				if(lstKpiPlanValue.get(staffId+"_"+KpiId) != null){			    					
//		    					
//		    		    		rUtils.addCell(sheet, 0, rowIndex, rowIndex, curFormatCenter);//stt
//		    		    		rUtils.addCell(sheet, 1, rowIndex, tmpStaff.getStaffCode(), curFormatLeft);//ma nv
//		    		    		rUtils.addCell(sheet, 2, rowIndex, tmpStaff.getStaffName(), curFormatLeft);//ten nv		    					    					
//		    					tmpKpiPlan = lstKpiPlanValue.get(staffId+"_"+KpiId);			  					
//			    				rUtils.addCell(sheet, 3, rowIndex,DateUtil.toDateString(tmpKpiPlan.getMonthPlan(), DateUtil.DATE_M_Y), curFormatCenter);//thang
//			    				rUtils.addCell(sheet, colIndex, rowIndex, (tmpKpiPlan.getValue()==null)?"":tmpKpiPlan.getValue(), curFormatRight);//value
////			    				isSuccess = true;
//		    				} else{
//		    					rUtils.addCell(sheet, 0, rowIndex, rowIndex, curFormatCenter);//stt
//		    		    		rUtils.addCell(sheet, 1, rowIndex, tmpStaff.getStaffCode(), curFormatLeft);//ma nv
//		    		    		rUtils.addCell(sheet, 2, rowIndex, tmpStaff.getStaffName(), curFormatLeft);//ten nv		    					    					
//			    				rUtils.addCell(sheet, colIndex, rowIndex, "", curFormatRight);//value
//		    				}	    				
//			    			colIndex++;
//		    			}		    			
//		    		}
//		    		
////		    		// if read data success 
////		    		if(isSuccess)
//	    			rowIndex ++;
//		    	}
//		    	
//		    }
//		    //data
//		    
//	    	//export
//	    	out = new FileOutputStream(exportFileName);
//	    	workbook.write(out);            
////	    	String outputPath = Configuration.getExportExcelPath() + outputName;
//	    	//Begin anhdt10 fix attt
//			String outputPath = outputName;
//			//End anhdt10
//	    	result.put(LIST, outputPath);
//	    	result.put(ERROR, false);
//			result.put("hasData", true);
//		
//			AbstractAction.isForExport = false;
//		}catch(Exception ex){
//			LogUtility.logError(ex, "ExportStaffKpi - " + ex.getMessage());			
//			result.put(ERROR, true);
//			result.put("errMsg", ValidateUtil.getErrorMsg(ConstantManager.ERR_SYSTEM));		
//		}finally {
//	    	if(out!=null){
//	    		try {
//					out.close();
//				} catch (IOException e) {					
//					LogUtility.logError(e, e.getMessage());
//				}				
//	    	}
//	    }
//		
//		return JSON;
//	}
	
//	public String exportRoutingKpi(List<Routing>lstRouting, List<Kpi>lstKpi, List<KpiPlan>lstKpiPlan){
//		FileOutputStream out = null;
//		SXSSFWorkbook workbook = null;
//		AbstractAction.isForExport = true;
//		try{
//			//Init XSSF workboook
//			//Begin anhdt10 -fix bug attt-  them usser Id vao ten file
//			Long  userId = currentUser==null?null:currentUser.getUserID();
//			String strUserId = userId==null? "UID" :userId.toString();
//			//End anhdt10
//			String name = ConstantManager.EXPORT_KPI_ROUTING_EN;
//			if(!StringUtil.isNullOrEmpty(mulLanguage) && (mulLanguage.equals("vn") || mulLanguage.equals("vi"))){
//				name = ConstantManager.EXPORT_KPI_ROUTING;
//			}
//			String outputName = DateUtil.toDateString(DateUtil.now(), DateUtil.DATE_FORMAT_EXCEL_FILE)+ "_" +name;
////			String exportFileName = Configuration.getStoreRealPath() + outputName;
//			//Begin anhdt10 - fix bug attt - them usser Id vao ten file
//			String exportFileName = Configuration.getStoreRealPath() + strUserId + "_"+  outputName;
//			//End anhdt10
//			workbook = new SXSSFWorkbook(-1);
//			String sheetName = Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "report.routing.kpi");
//		    SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(sheetName);
//		    Map<String, XSSFCellStyle> styles = ExcelProcessUtils.createStylesPOI(workbook);
//
//		    XSSFColor darkBlue40 = new XSSFColor(new java.awt.Color(83, 142, 213));
//			XSSFFont gridTitleFont = (XSSFFont)workbook.createFont();
//			gridTitleFont.setFontHeightInPoints((short)9);
//			gridTitleFont.setFontName("Arial");
//			gridTitleFont.setBold(true);
//			gridTitleFont.setColor(IndexedColors.WHITE.getIndex());
//			XSSFCellStyle gridTitleFormat = (XSSFCellStyle)workbook.createCellStyle();
//			gridTitleFormat.setFont(gridTitleFont);
//			gridTitleFormat.setAlignment(XSSFCellStyle.ALIGN_CENTER);
//			gridTitleFormat.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
//			gridTitleFormat.setFillForegroundColor(darkBlue40);
//			gridTitleFormat.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
//			gridTitleFormat.setBorderTop(BorderStyle.THIN);
//			gridTitleFormat.setTopBorderColor(IndexedColors.BLACK.getIndex());
//			gridTitleFormat.setBorderBottom(BorderStyle.THIN);
//			gridTitleFormat.setBottomBorderColor(IndexedColors.BLACK.getIndex());
//			gridTitleFormat.setBorderRight(BorderStyle.THIN);
//			gridTitleFormat.setRightBorderColor(IndexedColors.BLACK.getIndex());
//			gridTitleFormat.setWrapText(true);
//
//			XSSFColor lightBlue = new XSSFColor(new java.awt.Color(0, 176, 240));
//			XSSFCellStyle gridTitleFormat2 = (XSSFCellStyle)gridTitleFormat.clone();
//			gridTitleFormat2.setFillForegroundColor(lightBlue);
//
//			XSSFColor red = new XSSFColor(new java.awt.Color(218, 150, 148));
//			XSSFCellStyle gridTitleFormatRed = (XSSFCellStyle)gridTitleFormat.clone();
//			gridTitleFormatRed.setFillForegroundColor(red);
//			
//		    sheet.setDisplayGridlines(false);
//		    sheet.setDisplayZeros(false);
//		    //end init XSSF
//		    
//		    //Style
//	    	XSSFCellStyle curFormatLeft = styles.get(ExcelProcessUtils.DETAIL_NORMAL_DOTTED_LEFT);
//	    	XSSFCellStyle curFormatRight = styles.get(ExcelProcessUtils.DETAIL_NORMAL_DOTTED_RIGHT);
//	    	XSSFCellStyle curFormatCenter= styles.get(ExcelProcessUtils.DETAIL_NORMAL_DOTTED_CENTER);
//	    	
//		    //set Row Height - Column width
//		    rUtils.setRowsHeight(sheet, 0, 30,15);
//		    rUtils.setColumnsWidth(sheet, 0, 60, 150, 150, 150);
//		    
//		    
//		    //static menu
//		    String[] menu = Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "report.staff.kpi.menu.routing").split(",");
//		    for(int i=0; i<menu.length;i++){
//		    	rUtils.addCell(sheet, i, 0, menu[i], gridTitleFormat);
//		    }
//		    
//		    
//		    Kpi tmpKpi;
//		    //dynamic menu
//		    int colIndex = 4, rowIndex = 0;
//		    for(int i=0; i< lstKpi.size(); i++){
//		    	tmpKpi = lstKpi.get(i);
//		    	rUtils.setColumnWidth(sheet, colIndex, 150);
//		    	if(mulLanguage != null && (mulLanguage.equalsIgnoreCase("vn") || mulLanguage.equalsIgnoreCase("vi") )){
//		    		rUtils.addCell(sheet, colIndex, rowIndex, tmpKpi.getKpiNameVi(), gridTitleFormat);
//		    	}else{
//		    		rUtils.addCell(sheet, colIndex, rowIndex, tmpKpi.getKpiNameEn(), gridTitleFormat);
//		    	}
//		    	colIndex ++;
//		    }
//		    		    
//		    Long tmpkpiId, tmRoutingId;
//		    Kpi tmKpi;		    
//		    
//		    for(int j = 0 ; j < lstRouting.size() ; j++)
//		    {
//		    	Routing tmStaff = lstRouting.get(j);
//		    	tmRoutingId = tmStaff.getId();
//		    	int nullValue = 0;
//		    	  	
//		    	
//		    	for (int k = 0 ; k < lstKpi.size();k++)
//		    	{
//		    		KpiFilter filter = new KpiFilter();			    	
//		    		// set kpi_id
//		    		tmKpi = lstKpi.get(k);
//		    		tmpkpiId = tmKpi.getId();		    		
//		    		filter.setKpiId(tmpkpiId);		
//		    		
//		    		// set kpiType
//		    		filter.setKpiType(kpiType);
//		    		
//		    		// set shop_id
//		    		Shop shop = getCurrentShop();
//		    		filter.setShopId(shop.getId());		    		
//		    		// --- set routing_id --- 
//		    		filter.setRoutingId(tmRoutingId);		
//		    			    		
//		    		
//		    		List<KpiPlan> lst = kpiMgr.getListKpiPlanByFilter(filter);
//		    		if(lst==null || lst.size()==0){
//		    			nullValue ++;
//		    		}
//		    	}
//		    	
//		    	if(nullValue == lstKpi.size()){
//		    		lstRouting.remove(j);
//		    	}	    	
//		    	
//		    }
//		    
//		    KpiPlan tmpKpiPlan;
//		    HashMap<String, KpiPlan>lstKpiPlanValue = new HashMap<String, KpiPlan>();
//		    for(int i=0; i<lstKpiPlan.size(); i++){
//		    	tmpKpiPlan = lstKpiPlan.get(i);
//		    	
//		    	if(tmpKpiPlan != null)
//		    		
//		    		lstKpiPlanValue.put(tmpKpiPlan.getRouting().getId()+"_"+tmpKpiPlan.getKpi().getId(), tmpKpiPlan);
//		    }
//		    
//  
//		    		    
//		    //data
//		    //tao map data kpi plan
//		    Routing tmpRouting;
//		    
//		    colIndex = 4;
//		    rowIndex = 1;
//		    Long routingId, KpiId;
//		    
//		    for(int i=0; i<lstRouting.size(); i++){
//		    	tmpRouting = lstRouting.get(i);
//		    	if(tmpRouting != null){
//
//		    		routingId = tmpRouting.getId();
//	    			colIndex = 4;
////	    			boolean isSuccess = false;
//	    			int countExitsKPI = 0;
//	    			for(int j=0; j<lstKpi.size(); j++){
//		    			tmpKpi = lstKpi.get(j);
//		    			if(tmpKpi != null){
//		    				KpiId = tmpKpi.getId();
//		    				if(lstKpiPlanValue.get(routingId+"_"+KpiId) != null){
//		    					countExitsKPI++;
//		    					break;
//		    				}
//		    			}
//	    			}
//	    			if(countExitsKPI == 0){
//	    				continue;
//	    			}
//		    		for(int j=0; j<lstKpi.size(); j++){
//		    			tmpKpi = lstKpi.get(j);
//		    			if(tmpKpi != null){
//		    				KpiId = tmpKpi.getId();
//		    				if(lstKpiPlanValue.get(routingId+"_"+KpiId) != null){
//		    					tmpKpiPlan = lstKpiPlanValue.get(routingId+"_"+KpiId);
//		    					
//		    		    		rUtils.addCell(sheet, 0, rowIndex, rowIndex, curFormatCenter);//stt
//		    		    		rUtils.addCell(sheet, 1, rowIndex, tmpRouting.getRoutingCode(), curFormatLeft);//ma routing
//		    		    		rUtils.addCell(sheet, 2, rowIndex, tmpRouting.getRoutingName(), curFormatLeft);//ten tuyen		    		  		
//		    					rUtils.addCell(sheet, 3, rowIndex,DateUtil.toDateString(tmpKpiPlan.getMonthPlan(), DateUtil.DATE_M_Y), curFormatCenter);//thang
//			    				rUtils.addCell(sheet, colIndex, rowIndex, (tmpKpiPlan.getValue()==null)?"":tmpKpiPlan.getValue(), curFormatRight);//value
////			    				isSuccess = true;
//		    				} else{
//		    					rUtils.addCell(sheet, 0, rowIndex, rowIndex, curFormatCenter);//stt
//		    		    		rUtils.addCell(sheet, 1, rowIndex, tmpRouting.getRoutingCode(), curFormatLeft);//ma routing
//		    		    		rUtils.addCell(sheet, 2, rowIndex, tmpRouting.getRoutingName(), curFormatLeft);//ten tuyen		    		  		
//			    				rUtils.addCell(sheet, colIndex, rowIndex, "", curFormatRight);//value
//		    				}
//			    			colIndex++;
//		    				
//		    			}		    			
//		    		}
//		    		
////		    		// if read success line
////		    		if(isSuccess)
//	    			rowIndex ++;
//		    	}
//		    //	rowIndex ++;
//		    }
//		    //data
//		    
//	    	//export
//	    	out = new FileOutputStream(exportFileName);
//	    	workbook.write(out);            
////	    	String outputPath = Configuration.getExportExcelPath() + outputName;
//	    	//Begin anhdt10 fix attt
//			String outputPath = outputName;
//			//End anhdt10
//	    	result.put(LIST, outputPath);
//	    	result.put(ERROR, false);
//			result.put("hasData", true);
//		
//			AbstractAction.isForExport = false;
//		}catch(Exception ex){
//			LogUtility.logError(ex, "ExportStaffKpi - " + ex.getMessage());			
//			result.put(ERROR, true);
//			result.put("errMsg", ValidateUtil.getErrorMsg(ConstantManager.ERR_SYSTEM));		
//		}finally {
//	    	if(out!=null){
//	    		try {
//					out.close();
//				} catch (IOException e) {					
//					LogUtility.logError(e, e.getMessage());
//				}				
//	    	}
//	    }
//		
//		return JSON;
//	}
	
//public Boolean checkRowsDuplicate(List<String> row, List<List<String>> lstRows){
//		
//		String codeName = row.get(0).trim(); //column 1 : Routing or Staff code 
//		String month_plan = row.get(1).trim(); //column 2 : Month
//		
//		int count = 0;
//		for (List<String> isRow : lstRows){
//			String isCode = isRow.get(0).trim();
//			String isMonth = isRow.get(1).trim();
//			
//			if(isCode.compareToIgnoreCase(codeName) == 0 && isMonth.compareToIgnoreCase(month_plan) == 0){
//				count++ ;
//			}
//			
//			// xuat hien it nhat 2 lan
//			if(count == 2){
//				return true;
//			}
//		}		
//		return false;
//		
//	}
//	
//	
//	public String importKpi(){	
//		// important
//		boolean checkToken = this.csrfCheckTokenImportExcel();
//		if (!checkToken){
//			return PAGE_NOT_FOUND;
//		}
//		isError = false;
//		totalItem = 0;
//		String message = "";
//		String enterNewLine="\n";
//		
//		if(staffSign==null || staffSign.getShop()==null){
//			return SUCCESS;
//		}
//						
//		Long shopId = staffSign.getShop().getId();	
//		Shop shop = staffSign.getShop();
//	//	Staff staff = new Staff();
//	    Integer type = staff.getStaffType().getObjectType(); //(staff.getStaffType().getObjectType() == StaffObjectType.ADMIN.getValue())?1:(staff.getStaffType().getObjectType() == StaffObjectType.GS.getValue())?2:null;//lochp
//
//		
//		Date createDate = DateUtil.now();
//		Routing rout = new Routing();
//		Staff staffImport = new Staff();
//		Date MonthPlan = DateUtil.getSysdateFromDatabase();
//		KpiPlan kpiPlan ;
//		
//		List<KpiPlan> lstKpiPlan= new ArrayList<KpiPlan>(); 	
//		List<CellBean> lstFails = new ArrayList<CellBean>();
//		
//		// -------- Get other columns name from table KPI ----------------
//		int nbColumns = 0;		
//		KpiFilter filter = new KpiFilter();
//		filter.setShopId(shopId);				
//		try {
//			List<Kpi> lstKpi = kpiMgr.getLisKpiByFilter( filter);
//			nbColumns = lstKpi.size();
//		} catch (BusinessException e) {
//			isError = true;
//			message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.column.not.exist") + enterNewLine;
//			return SUCCESS;
//		}				
//				
//				
//		// we have columns : Code routing or staff | month plan | n columns from table KPI		
//		List<List<String>> lstData = getExcelData(excelFile, message, errMsg, nbColumns+2, true);
//		if (isError){
//			errMsg = super.errMsg;
//			return SUCCESS;
//		}
//		// --------- Get informations from header ----------------------- 
//		List<String> saveHeader = new ArrayList<String>();
//		for(int i = 0 ; i < lstData.get(0).size() ; i++){			
//			saveHeader.add(lstData.get(0).get(i));
//		}
//					
//		
//		List<String> header = lstData.get(0); // get name column
//		header.remove(0); // remove Routing or Staff code
//		header.remove(0); // remove Month
//		
//		List<Kpi> lstTypeKpi = new ArrayList<Kpi>();
//		
//		// name column 
//		for(int i=0;i< header.size();i++)
//		{
//			KpiFilter k = new KpiFilter();
//			k.setKpiName(header.get(i));
//			if(mulLanguage != null && (mulLanguage.equalsIgnoreCase("vn") || mulLanguage.equalsIgnoreCase("vi"))){
//				k.setKpiNameVi(header.get(i));
//				k.setLanguage("vn");
//			}else{
//				k.setKpiNameEn(header.get(i));
//				k.setLanguage("en");
//			}
//			k.setShopId(shopId);
//			k.setActiveType(ActiveType.RUNNING);			
//			k.setImportFlag(true);
//			try {
//				List<Kpi> lstKpi= kpiMgr.getLisKpiByFilter(k);
//				if(lstKpi != null && lstKpi.size() > 0){
//					lstTypeKpi.add(lstKpi.get(0));
//				}
//				
//			} catch (BusinessException e1) {
//				isError = true;
//				message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.column.not.exist",header.get(i)+ enterNewLine);	
//			}	 
//
//		}
//		// Finish get informations from header		
//		
//		lstData.remove(0);// remove header
//		
//		// Check routing or staff
//		Boolean routing = null ;
//		ConfigValueMapping cfUseOrderModule;
//		try {
//			cfUseOrderModule = configMgr.getConfigValueMappingByShopAndConfigCode(shopId, ConfigType.CF_AMOUNT_CAL_BY_OBJECT.getValue());			
//			if (cfUseOrderModule != null){
//				if (Integer.parseInt(cfUseOrderModule.getValue()) == 1){ // NVBH	
//					routing = false;
//				} else if (Integer.parseInt(cfUseOrderModule.getValue()) == 2){	// TUYEN
//					routing = true;
//				}
//			}				
//		} catch (BusinessException e1) {
//			isError = true;
//			message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.code.not.exist")+ enterNewLine;	
//		}
//
//		
//		// Trait the lines data 	
//		setLstView(new ArrayList<CellBean>());
//		if (lstData != null && lstData.size() == 0){
//			isError = true;
//			errMsg = R.getResource("error.msg.imported.file.is.empty");
//		}
//		if (StringUtil.isNullOrEmpty(errMsg) && lstData!= null && lstData.size() > 0) {			
//			try{				
//				
//				ObjectVO<Routing> lstR = new ObjectVO<Routing>();	
//				ObjectVO<Staff> lstS = new ObjectVO<Staff>();
//				if(routing){ // routing
//					if(type==StaffObjectType.GS.getValue()){//anhhpt : kiem tra gs import file excel
//						lstR = salePlanMgr.getListRoutingByUserLogin(null,staffSign.getId(),shop.getId(), null, null, type,getChooseGroup().getId());
//					}else{
//						 lstR = salePlanMgr.getListRoutingByUserLoginNoCheckStaffGroup(null,staffSign.getId(),shop.getId(),null,null,type);
//					}
//					if(type==StaffObjectType.GS.getValue()){//anhhpt : kiem tra gs import file excel
//						lstR = salePlanMgr.getListRoutingByUserLogin(null,staffSign.getId(),shop.getId(), null, null, type,getChooseGroup().getId());
//					}else if (type == StaffType.QL.getValue()){
//						 lstR = salePlanMgr.getListRoutingByUserLogin(null,staffSign.getId(),shop.getId(), null, null, type, null);
//					}else {
//						lstR = salePlanMgr.getListRoutingByUserLoginNoCheckStaffGroup(null,staffSign.getId(),shop.getId(),null,null,type);
//					}
//				}else{// nvbh
//					if(type==StaffObjectType.GS.getValue()){//anhhpt : kiem tra gs import file excel
//						lstS = salePlanMgr.getListStaffPreSalesAndVanSalesEx(null,staffSign.getId(), shop.getId(), null, null, Arrays.asList(staffSign.getId()),getChooseGroup().getId());
//					}else{
//						lstS = salePlanMgr.getListStaffPreSalesAndVanSales(null,staffSign.getId(),shop.getId(),null,null,type,null);	
//					}
//				}
//				
//				
//				//Begin Process Detail Row
//				for (int i=0, iSize = lstData.size(); i< iSize; i++) {
//					List<String> row = lstData.get(i);	
//					message = "";					
//					if(checkRowsDuplicate(row,lstData)){//Count Row to check Duplicate
//						isError = true;
//						String month = Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.rate.template.file.header.month");
//						String code = "";
//						if(routing){
//							code = Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.rate.template.file.header.routing");	
//						} else{
//							code = Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.rate.template.file.header.staff");
//						}						 
//						message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.line.exist",code,month)+ enterNewLine;
//					} else { // this line is unif
//					   	Boolean isNVBH=true;
//						
//						if (row != null && row.size() > 0) {
//							String CodeName = row.get(0); //column 1 : Routing or Staff code 
//							String month_plan = row.get(1); //column 2 : Month					 						
//							
//							// get data from column other
//							List<String> lstDataColumn = new ArrayList<String>();
//							for(int j=0;j< header.size();j++){
//								lstDataColumn.add(row.get(2+j));
//							}
//												
//							// Coding for column 1 : Routing or staff code   
//							if (StringUtil.isNullOrEmpty(CodeName)) {
//								isError = true;
//								
//								if(routing){
//									message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.routing.code.not.empty")+ enterNewLine;	
//								}
//								else{
//									message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.staff.not.empty")+ enterNewLine;	
//								}				
//								
//							} else {					
//								
//								if(routing){ // TUYEN
//									String routingCode = CodeName.trim();																			
//									rout = kpiMgr.getRoutingByShopId(shopId,routingCode.trim());		
//									if(rout == null){
//										isError = true;
//										message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.routing.code.not.empty")+ enterNewLine;	
//									}else{
//										
//										if(lstR != null && lstR.getLstObject() != null && lstR.getLstObject().size() >0){
//											List<Long> lstId = new ArrayList<Long>();
//											for(Routing rt:lstR.getLstObject()){
//												lstId.add(rt.getId());
//											}
//											if(lstId.indexOf(rout.getId()) == -1){
//												message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE,"sale.plan.month.distribute.excel.routing.not.allow",routingCode);
//												message += "\n";
//											}
//										}else{
//											message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE,"sale.plan.month.distribute.excel.routing.not.allow",routingCode);
//											message += "\n";
//										}
//										if(rout != null){
//											long rId = rout.getShop().getId();
//											long sId = shop.getId();
//											if(!ActiveType.RUNNING.equals(rout.getStatus())){
//												message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE,"sale.plan.month.distribute.excel.routing",routingCode);
//												message += "\n";
//											} else if(rId != sId){
//												message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE,"sale.plan.month.distribute.excel.routing.not.in.shop");
//												message += "\n";
//											}
//										}
//									}
//								}
//								else{ // NV
//									String staffCode = CodeName.trim();
//									staffImport = staffMgr.getStaffByShopAndStaffCode(shopId, staffCode.trim());
//																
//									if(staffImport == null){
//										isError = true;
//										message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.staff.not.empty")+ enterNewLine;	
//									}
//									else{
//										// Kiem tra nv ban hang 
//										StaffFilter staffFilter = new StaffFilter();
//										StaffGroup staffGroup = getChooseGroup();
//										if(staffGroup!=null){									
//											staffFilter.setStaffGroupId(staffGroup.getId());
//										}										
//										staffFilter.setShopId(staffImport.getShop().getId());
//										staffFilter.setListStaffType(Arrays.asList(StaffType.NVBH,StaffType.NVBH_VANSALE));
//										staffFilter.setStaffCode(staffCode.trim());								
//										
//										ObjectVO<Staff>  objectVO = staffMgr.getListStaffByFilter(staffFilter);
//										List<Staff> listStaff = objectVO.getLstObject();
//										
//										if(listStaff == null || listStaff.size()==0){
//											isNVBH = false;
//											isError = true;
//											message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.staff.not.nvbh",staffCode)+ enterNewLine;	
//										}
//										if(lstS != null && lstS.getLstObject() != null && lstS.getLstObject().size() >0){
//											List<Long> lstId = new ArrayList<Long>();
//											for(Staff st:lstS.getLstObject()){
//												lstId.add(st.getId());
//											}
//											
//											if(lstId.indexOf(staffImport.getId()) == -1){
//												message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE,"sale.plan.month.distribute.excel.staff.not.allow",staffCode);
//												message += "\n";
//											}
//										}else{
//											message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE,"sale.plan.month.distribute.excel.staff.not.allow",staffCode);
//											message += "\n";
//										}
//										if(!ActiveType.RUNNING.equals(staffImport.getStatus())){
//											message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE,"sale.plan.month.distribute.excel.staff",staffCode);
//											message += "\n";
//										} 
//										
//										if(!shop.getId().equals(staffImport.getShop().getId())){
//											message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "common.not.shop.exist",Configuration.getResourceString(ConstantManager.VI_LANGUAGE,"catalog.staff.code"));
//											message += "\n";
//										}
//																													
//									}
//										
//								}															
//							}
//											
//							
//							// Coding for column 2 : Thang 
//							if (StringUtil.isNullOrEmpty(month_plan)) {
//								isError = true;
//								message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.month.plan.not.empty")+ enterNewLine;
//							} else {								
//								
//								if(isNVBH){
//									 try{
//										 Date date  = DateUtil.parse(month_plan, DateUtil.DATE_M_Y);
//										 String montPlanStr = "01/" + DateUtil.toDateString(date, DateUtil.DATE_M_Y);
//										 MonthPlan = DateUtil.parse(montPlanStr, DateUtil.DATE_FORMAT_STR);			
//														 								
//										 if (!checkMonthInKpi(lstTypeKpi.get(i), MonthPlan)){
//											 isError = true;
//											 message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.date.invalidat")+ enterNewLine;
//										 }
//										 
//										 String msg = ValidateUtil.getErrorMsgForInvalidFormatShortDate(month_plan,Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.date.format"), true);
//										 message += msg;
//									 }catch(Exception ex){							
//										 isError = true;
//										 message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.date.format")+ enterNewLine;
//									 }
//									 
//								}
//								
//								 
//							}
//							
//													
//							// read table KPI, params : shopId, Type --> get KPI_ID
//							
//							// get data from columns : present, we have 
//								//column [3] : so Don Hang 
//								//column [4] : Sku
//								//column [5] : Doanh so
//								//column [6] : San Luong										
//							for(int j=0;j< header.size() && isNVBH;j++){																
//								if ( !StringUtil.isNullOrEmpty(lstDataColumn.get(j)) ) {	
//									if(!StringUtil.isNumeric(lstDataColumn.get(j))){	//khong phai la kieu so 								
//										 isError = true;
//										 message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.data.characte",header.get(j))+ enterNewLine;
//									}else if(Double.parseDouble(lstDataColumn.get(j)) <= 0){ // khong la so am 									
//										 isError = true;
//										 message += Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.file.data.negative",header.get(j))+ enterNewLine;
//									} else{
//										
//										message += ValidateUtil.validateField(lstDataColumn.get(j),"jsp.khuyenmai.soluong", 8,ConstantManager.ERR_INTEGER,ConstantManager.ERR_MAX_LENGTH);								
//										
//										if(StringUtil.isNullOrEmpty(message)){
//											
//											kpiPlan = new KpiPlan();																			
//											kpiPlan.setKpi(lstTypeKpi.get(j));
//											kpiPlan.setShop(shop);							
//											kpiPlan.setCreateDate(createDate);									
//											kpiPlan.setMonthPlan(MonthPlan);
//											kpiPlan.setValue( new BigDecimal(lstDataColumn.get(j)));								
//											
//											if(routing){ // TUYEN
//												kpiPlan.setRouting(rout);
//											}
//											else{ // NVBH
//												kpiPlan.setStaff(staffImport);
//											}																						
//										  // save table KPI_PLAN : shopId, KPI_ID, value 							
//											lstKpiPlan.add(kpiPlan);	
//										}								
//									
//									}												
//								}
//								
//							}											
//						}
//					}			
//					
//					
//					if( !StringUtil.isNullOrEmpty(message)){
//						lstFails.add(StringUtil.addFailBean(row, message));
//					}
//					else{					
//						kpiMgr.createOrUpdateListKpiPlan(shopId, lstKpiPlan, getLogInfoVO());
//						lstKpiPlan.clear();				
//					}
//							
//					totalItem++;						
//				}		
//				// if the data fail, give file excel errors for user  		
//				if(mulLanguage != null && (mulLanguage.equalsIgnoreCase("vn") || mulLanguage.equalsIgnoreCase("vi"))){
//					getOutputFailExcelFileDynamic(saveHeader,lstFails,ConstantManager.TEMPLATE_KPI_IMPORT_FAIL);
//				}else{
//					getOutputFailExcelFileDynamic(saveHeader,lstFails,ConstantManager.TEMPLATE_KPI_IMPORT_FAIL_EN);
//				}
//				
//			}catch(Exception ex)
//			{
//				LogUtility.logErrorStandard(ex, ex.getMessage(), logInfo);
//				errMsg = ValidateUtil.getErrorMsg(ConstantManager.ERR_SYSTEM);
//			}
//		}
//			
//		if (StringUtil.isNullOrEmpty(errMsg)) {
//			isError = false;
//		}
//		return SUCCESS;
//	}
//	
//	private Boolean checkMonthInKpi(Kpi k, Date month){
//		if (k != null && GroupUtility.checkNumberMoreZero(k.getId())){
//			KpiFilter filter = new KpiFilter();
//			filter.setShopId(getCurrentShopId());
//			filter.setKpiId(k.getId());
//			filter.setImportFlag(true);
//			if (month != null){
//				filter.setMonthPlan(DateUtil.toDateString(month, DateUtil.DATE_FORMAT_STR));
//			}
//			try {
//				KpiShop kp = kpiMgr.getKpiShopByKpiAndShopId(filter);
//				if (kp != null){
//					return true;
//				}
//			} catch (BusinessException e) {
//				LogUtility.logError(e, e.getMessage());
//			}
//		}
//		return false;
//	}
//	
//	public void getOutputFailExcelFileDynamic(List<String> saveHeader,List<CellBean> lstFails,String fileName)
//	{
//		FileOutputStream out = null;
//		SXSSFWorkbook workbook = null;
//		numFail = lstFails.size();
//		
//		if(numFail == 0){
//			return;
//		}
//		
//		try{
//			//Begin anhdt10 -fix bug attt-  them usser Id vao ten file
//			Long  userId = currentUser==null?null:currentUser.getUserID();
//			String strUserId = userId==null? "UID" :userId.toString();
//			//End anhdt10
//			//Init XSSF workboook
//			String outputName =  DateUtil.toDateString(DateUtil.now(), DateUtil.DATE_FORMAT_EXCEL_FILE)+ "_" + fileName ;
////			String exportFileName = Configuration.getStoreRealPath() + outputName;
//			//Begin anhdt10 - fix bug attt - them usser Id vao ten file
//			String exportFileName = Configuration.getStoreRealPath() + strUserId + "_"+  outputName;
//			//End anhdt10
//			workbook = new SXSSFWorkbook(-1);
//			String sheetName = Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.export.data.erros");
//		    SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(sheetName);
//		    Map<String, XSSFCellStyle> styles = ExcelProcessUtils.createStylesPOI(workbook);
//
//		    XSSFColor darkBlue40 = new XSSFColor(new java.awt.Color(83, 142, 213));
//			XSSFFont gridTitleFont = (XSSFFont)workbook.createFont();
//			gridTitleFont.setFontHeightInPoints((short)9);
//			gridTitleFont.setFontName("Arial");
//			gridTitleFont.setBold(true);
//			gridTitleFont.setColor(IndexedColors.WHITE.getIndex());
//			XSSFCellStyle gridTitleFormat = (XSSFCellStyle)workbook.createCellStyle();
//			gridTitleFormat.setFont(gridTitleFont);
//			gridTitleFormat.setAlignment(XSSFCellStyle.ALIGN_CENTER);
//			gridTitleFormat.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
//			gridTitleFormat.setFillForegroundColor(darkBlue40);
//			gridTitleFormat.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
//			gridTitleFormat.setBorderTop(BorderStyle.THIN);
//			gridTitleFormat.setTopBorderColor(IndexedColors.BLACK.getIndex());
//			gridTitleFormat.setBorderBottom(BorderStyle.THIN);
//			gridTitleFormat.setBottomBorderColor(IndexedColors.BLACK.getIndex());
//			gridTitleFormat.setBorderRight(BorderStyle.THIN);
//			gridTitleFormat.setRightBorderColor(IndexedColors.BLACK.getIndex());
//			gridTitleFormat.setWrapText(true);
//
//			XSSFColor lightBlue = new XSSFColor(new java.awt.Color(0, 176, 240));
//			XSSFCellStyle gridTitleFormat2 = (XSSFCellStyle)gridTitleFormat.clone();
//			gridTitleFormat2.setFillForegroundColor(lightBlue);
//
//			XSSFColor red = new XSSFColor(new java.awt.Color(218, 150, 148));
//			XSSFCellStyle gridTitleFormatRed = (XSSFCellStyle)gridTitleFormat.clone();
//			gridTitleFormatRed.setFillForegroundColor(red);
//			
//		    sheet.setDisplayGridlines(false);
//		    sheet.setDisplayZeros(false);
//		    //end init XSSF
//		    
//		    //Style
//	    	XSSFCellStyle curFormatLeft = styles.get(ExcelProcessUtils.DETAIL_NORMAL_DOTTED_LEFT);
//	    	XSSFCellStyle curFormatRedFontLeft = styles.get(ExcelProcessUtils.DETAIL_NORMAL_RED_FONT_DOTTED_LEFT);
//	    	XSSFCellStyle curFormatRight = styles.get(ExcelProcessUtils.DETAIL_NORMAL_DOTTED_RIGHT);
//	    	XSSFCellStyle curFormatCenter= styles.get(ExcelProcessUtils.DETAIL_NORMAL_DOTTED_CENTER);
//	    	
//		    //set Row Height - Column width
//		    rUtils.setRowsHeight(sheet, 0, 30,15);
//		    rUtils.setColumnsWidth(sheet, 0, 60, 150, 150, 150);
//		    
//    
//	
//		     Begin  Dynamic header 	
//		    int rowIndex = 0;
//		    int colIndex=0;
//		    String monthStr = Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "jsp.khtt.export.import.temp.thang.nam");
//		    Integer monthIndex = null;
//		    for(colIndex=0; colIndex < saveHeader.size(); colIndex++){
//		    	rUtils.setColumnWidth(sheet, colIndex, 150);
//		    	rUtils.addCell(sheet, colIndex, rowIndex,  saveHeader.get(colIndex), gridTitleFormat);
//		    	if (!StringUtil.isNullOrEmpty(monthStr) && !StringUtil.isNullOrEmpty(saveHeader.get(colIndex))){
//		    		if (monthStr.equalsIgnoreCase(saveHeader.get(colIndex))){
//		    			monthIndex = colIndex;
//		    		}
//		    	}
//		    }
//		    
//		    	// add colums information error 
//		    rUtils.setColumnWidth(sheet, colIndex, 150);
//		    String infosErrors= Configuration.getResourceString(ConstantManager.VI_LANGUAGE, "kpi.import.data.erros");
//		    rUtils.addCell(sheet, colIndex, rowIndex, infosErrors, gridTitleFormat);
//		     Finish Dynamic header 
//		    
//	    
//		     Begin : trait data on line
//		   	
//		    // rUtils.addCell(sheet, colIndex, rowIndex,  saveHeader.get(colIndex), gridTitleFormat)
//		    
//		    rowIndex = 1;
//		    Integer headerSize = saveHeader.size();
//		    for(int i=0; i < lstFails.size(); i++){ 
//		    	CellBean cell = lstFails.get(i);
//		    	colIndex=0;
//		    	
//		    	// Add data input  
//		    	for( ; colIndex <  headerSize; colIndex++) {  
//		    		rUtils.setColumnWidth(sheet, colIndex, 150);
//		    		if (monthIndex != null && monthIndex == colIndex){
//		    			rUtils.addCell(sheet, colIndex, rowIndex, cell.getContentIndex(colIndex+1), curFormatCenter);
//		    		}else if( monthIndex != null && colIndex > monthIndex && headerSize > 1 && colIndex < headerSize){
//		    			rUtils.addCell(sheet, colIndex, rowIndex, cell.getContentIndex(colIndex+1), curFormatRight);
//		    		}else{
//		    			rUtils.addCell(sheet, colIndex, rowIndex, cell.getContentIndex(colIndex+1), curFormatLeft);
//		    		}
//		    	}
//		    	
//		    	// Add errors messages
//	    		rUtils.setColumnWidth(sheet, colIndex, 250);
//	    		rUtils.addCell(sheet, colIndex, rowIndex, cell.getErrMsg(), curFormatRedFontLeft);
//	    		
//		    	rowIndex ++;
//		    }
//		     Finish : trait data on line
//		    
//		    
//		    
//	    	//export
//	    	out = new FileOutputStream(exportFileName);
//	    	workbook.write(out);     	    	
////			fileNameFail = Configuration.getExportExcelPath() + outputName;
//			//Begin anhdt10 fix attt
//			fileNameFail = outputName;
//			//End anhdt10
//
//		}catch(Exception ex){
//			LogUtility.logError(ex, "ExportKpiErrors - " + ex.getMessage());			
//			result.put(ERROR, true);
//			result.put("errMsg", ValidateUtil.getErrorMsg(ConstantManager.ERR_SYSTEM));		
//		}finally {
//	    	if(out!=null){
//	    		try {
//					out.close();
//				} catch (IOException e) {					
//					LogUtility.logError(e, e.getMessage());
//				}				
//	    	}
//	    }
//	}
	
}

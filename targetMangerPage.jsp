<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<div class="BreadcrumbSection">
    <ul class="ResetList FixFloat BreadcrumbList">
        <li class=""><a href="javascript:void(0)"><s:text name="jsp.plan.menu"></s:text></a></li>
        <li><span> <s:text name="target.title.setting"></s:text> </span></li>
    </ul>
</div>
<div class="CtnOneColSection">
    <div class="ContentSection">
    	<div class="ToolBarSection">
            <div class="SearchSection GeneralSSection">
            	<h2 class="Title2Style"><s:text name="jsp.common.thongtintimkiem"></s:text></h2>
                <div class="SearchInSection SProduct1Form" id="searchForm">
               					
					<label class="LabelStyle Label1Style" id="calendarM2"><s:text name="jsp.common.month"></s:text><span class="ReqiureStyle">*</span></label> 
					<input type="text" class="InputTextStyle InputText1Style" autocomplete="off" style="width: 100px;height:26px;" id="searchMonth" maxlength="7"  onchange="return changeDateCreate();"/> 
						
					<s:if test="targetType == 1">
						<label id="lbCbTitle" class="LabelStyle Label1Style"><s:text name="jsp.common.staff.code"></s:text>(F9)</label>
						<input id="staffCode" type="text" class="InputTextStyle InputText6Style"  maxlength="150"/>
					</s:if>
					<s:else>
						<label id="lbCbTitle" class="LabelStyle Label1Style"><s:text name="catalog.routing.code"></s:text>(F9)</label>
						<input id="routingCode" type="text" class="InputTextStyle InputText6Style"  maxlength="150"/>
					</s:else>				
					
					<div id="searchStyleProductForTree" style="display: none;">
					<div id="searchStyleProductForTreeDialog" class="easyui-dialog" title=""
						data-options="closed:true,modal:true">
						<div class="PopupContentMid2">
							<div class="GeneralForm Search1Form">
								<label id="seachStyleProductCodeLabel"
									class="LabelStyle Label2Style"><s:text name="jsp.common.customer"></s:text>
								</label> 
								<input id="seachStyleProductCode" type="text" maxlength="250"
									tabindex="101" class="InputTextStyle InputText5Style" /> 
								<label id="seachStyleProductNameLabel" class="LabelStyle Label2Style">
									<s:text name="jsp.common.customer"></s:text>
								</label> 
								<input id="seachStyleProductName" type="text" maxlength="250"
									tabindex="101" class="InputTextStyle InputText5Style" /> 
								<div class="Clear"></div>
								<div class="BtnCenterSection">
									<button class="BtnGeneralStyle Sprite2 BtnSearchOnDialog"
										tabindex="102" id="btnSearchStyleProduct" style="margin-left: 30px">
										<span class="Sprite2"><s:text name="jsp.common.timkiem"></s:text></span>				
									</button>
								</div>
				
								<div class="Clear"></div>
							</div>
							<div class="GeneralForm Search1Form">
								<div class="GridSection" id="searchStyleProductContainerGrid">
									<table id="gridProductDialog"></table>
								</div>
				
							</div>
							<div class="Clear"></div>
							<div class="GeneralForm Search1Form" style="text-align: center">
								<button id="btSearchForm" class="BtnGeneralStyle Sprite2 BtnSearchOnDialog"
									onclick="$('#searchStyleProductForTreeDialog').dialog('close');" tabindex="103">
									<span class="Sprite2"><s:text name="jsp.common.dong"></s:text></span>
								</button>
							</div>
							<div class="Clear"></div>
							<p id="errMsgDialog" class="ErrorMsgStyle" style="display: none;"></p>
				
						</div>
					</div>
				</div>


					
										
					<button id="btnSearch" onclick="return TargetManger.initData(true);" class="BtnGeneralStyle" style="margin-left: 10px;font-size:12px;height: 28px;width:85px;">
						<s:text name="jsp.common.timkiem"></s:text>
					</button>
					<div class="Clear"></div>	
					<p id="errMsg" class="ErrorMsgStyle" style="display: none"></p>
                </div>
                
                
                <s:if test="targetType == 1">
					<h2 class="Title2Style"><s:text name="jsp.common.list.staff"></s:text></h2> 
				</s:if>
				<s:else>
					<h2 class="Title2Style"><s:text name="jsp.target.list.route"></s:text></h2> 
				</s:else>
                
                
                <div class="SearchInSection SProduct1Form">
                <div class="GridSection" >
                	<div class="ResultSection">
							<table id="grid"></table>
					</div>
                </div>                	
                    <label class="LabelStyle Label1Style">
                    	<s:text name="jsp.common.month"></s:text><span class="ReqiureStyle">*</span>
                    </label>
	                <input id="dateCreate" type="text" class="InputTextStyle InputText6Style" onchange="return validateMonth($(this));" readonly="readonly"/>
	            	<div class="BtnCenterSection">
	            		<button id="btnDistribute" class="BtnGeneralStyle BtnMSection" onclick="return TargetManger.saveData(1);">
	            			<span class="Sprite2"><s:text name="jsp.target.do.allocate"></s:text></span>
	            		</button>
	            		<button id="btnReset" class="BtnGeneralStyle" onclick="return TargetManger.resetData();">
	            			<span class="Sprite2"><s:text name="jsp.target.reset.data"></s:text></span>
	            		</button>
	            		<img id="loadingCreate" class="LoadingStyle" style="visibility: hidden;" src="/resources/images/loading.gif">
	        		</div>
	        		<div class="Clear"></div>
	        		<div class="GeneralForm GeneralNoTP1Form">
						<div class="Func1Section">
							<p class="DownloadSFileStyle DownloadSFile2Style">
								<a id="downloadTemplate" href="javascript:void(0)"><s:text name="jsp.common.download.template.file.excel"></s:text></a>
							</p>		
							<div class="DivInputFile">
									<form action="/target-manager/import" name="importFrm" id="importFrm"  method="post" enctype="multipart/form-data">
										<input type="file" class="InputFileStyle" size="20" name="excelFile" id="excelFile" onchange="previewImportExcelFile(this,'importFrm');">
						            <div class="FakeInputFile">
						                <input id="fakefilepc" type="text" class="InputTextStyle InputText1Style">
						            </div>
								</form>
						       </div>
						        <button id="btnImport" class="BtnGeneralStyle" onclick="return TargetManger.importFile();"><s:text name="jsp.common.import.from.file"></s:text></button>
							    <button id="btnExport" class="BtnGeneralStyle" onclick="return TargetManger.exportExcel();"><s:text name="jsp.common.export.to.file"></s:text></button>
							    <div class="Clear"></div>
							</div>
					    <div class="Clear"></div>
					</div>
					<div class="Clear"></div>
				    <p id="errExcelMsg" class="ErrorMsgStyle" style="display: none"></p>
	          		<p id="mainErr" class="ErrorMsgStyle" style="display: none;"></p>
	          		<p id="successMsg" class="SuccessMsgStyle" style="display: none"></p>
	          		<input type="hidden" id="routingDialogId" value=""/>
					<input type="hidden" id="staffDialogId" value=""/>
	          		
	          		<tiles:insertTemplate template="/WEB-INF/jsp/general/successMsg.jsp" />            	
                </div>
            </div>
            <div class="Clear"></div>
        </div>
    </div>
    <div class="Clear"></div>
</div>
<s:hidden id="isSupervisor" name="isSupervisor"></s:hidden>



<script>
	$(document).ready(function() {
		
		
		$('#searchForm #staffCode, #routingCode').live('keyup',function(e){
			if(e.keyCode == keyCodes.ENTER){
				$('#searchForm #btnSearch').click();
			}
		});
		
		$('#downloadTemplate').attr('onclick', 'return TargetManger.exportExcelTemplate()');
		$('#excelFile').css('width', '196px');
		var options = { 
 			beforeSubmit: TargetManger.beforeImportExcel,   
 	 		success:      TargetManger.afterImportExcelUpdate,
 	 		type: "POST",
 	 		dataType: 'html',
 	 		data: {token: $('#token').val()}
		}; 
		$('#importFrm').ajaxForm(options);
		

		TargetManger.pagePlanType = Number('<s:property value="targetType" />');
		applyMonthPicker("searchMonth");
		$('#searchMonth').val(DateUtils.getCurrentMonth());
		applyMonthPicker("dateCreate");
		$('#dateCreate').val(DateUtils.getCurrentMonth());
		
		TargetManger.initData();
		console.log("da goi initData()");

	});
	
	function changeDateCreate(){
		$('#dateCreate').val($('#searchMonth').val());
		$('#dateCreate').change();
	}
	function validateMonth(obj){
		if(!isNullOrEmpty(obj.val())){			
			var startMonth = obj.val().trim();
			var endMonth = DateUtils.getCurrentMonth();
			if(DateUtils.compareMonth(endMonth,startMonth)>0){					
				$('#btnDistribute').attr('disabled','disabled');
				$('#btnDistribute').addClass('BtnGeneralDStyle');					
				$('#btnReset').attr('disabled','disabled');
				$('#btnReset').addClass('BtnGeneralDStyle');					
				$('.Value').attr('disabled','disabled');
			}else{					
				$('#btnDistribute').removeAttr('disabled');
				$('#btnDistribute').removeClass('BtnGeneralDStyle');
				
				$('#btnReset').removeAttr('disabled');
				$('#btnReset').removeClass('BtnGeneralDStyle');					
				$('.Value').removeAttr('disabled');
			}
		}else{				
			$('#btnDistribute').removeAttr('disabled');
			$('#btnReset').removeAttr('disabled');				
			$('.Value').removeAttr('disabled');
		}
	}
	
	CreateSalePlan.isSupervisor = ('<s:property value="isSupervisor"/>'=='true')?true:false;
	CreateSalePlan.loginStaffId = '<s:property value="loginStaffId"/>';
	$('#staffCode').bind('keyup', function(event){
		if(event.keyCode == keyCode_F9){
			TargetManger.staffGroupId = true;
			CreateSalePlan.openDialogSearchShop(1);
		}

	}).focus();
	
	$('#routingCode').bind('keyup', function(event){
		if(event.keyCode == keyCode_F9){
			TargetManger.staffGroupId = true;
			CreateSalePlan.openDialogSearchShop(2);
		}

	}).focus();
	
	var DMSI18N = {
			jsp_common_total: '<s:text name="jsp.report.sale.plan.sum"></s:text>'
			,jsp_common_route_code: '<s:text name="jsp.common.route.code"></s:text>'
			,jsp_target_sale_staff_code: '<s:text name="jsp.target.sale.staff.code"></s:text>'
			,jsp_common_sale_staff_1: '<s:text name="jsp.common.sale.staff.1"></s:text>'
			,jsp_common_route_name: '<s:text name="jsp.common.route.name"></s:text>'
			,jsp_common_month: '<s:text name="jsp.common.month"></s:text>'
			,jsp_target_import_success_x_rows_fail_x_rows: '<s:text name="jsp.target.import.success.x.rows.fail.x.rows"></s:text>'
			,jsp_target_err_detail: '<s:text name="jsp.target.err.detail"></s:text>'
			,jsp_target_export_file_unccessfuly_error_x: '<s:text name="jsp.target.export.file.unccessfuly.error.x"></s:text>'
			,jsp_common_no_data: '<s:text name="jsp.common.no.data"></s:text>'
			,jsp_common_input_positive_number: '<s:text name="jsp.common.input.positive.number"></s:text>'
			,jsp_common_input_integer: '<s:text name="jsp.common.input.integer"></s:text>'
			,jsp_target_allocate_value_must_greater_than_zero: '<s:text name="jsp.target.allocate.value.must.greater.than.zero"></s:text>'
			,jsp_target_allocate_month: '<s:text name="jsp.target.allocate.month"></s:text>'
			,jsp_target_only_allow_allocate_month_from_now_on: '<s:text name="jsp.target.only.allow.allocate.month.from.now.on"></s:text>'
			,jsp_target_search_month_different_to_allocate_month_wanna_continue_allocate: '<s:text name="jsp.target.search.month.different.to.allocate.month.wanna.continue.allocate"></s:text>'
			,jsp_common_wanna_export_excel: '<s:text name="jsp.common.wanna.export.excel"></s:text>'
			
	}
	
</script>


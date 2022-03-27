package dev.vsuarez.preshipment.process;

import java.math.BigDecimal;
import java.util.List;

import org.compiere.model.MDocType;
import org.compiere.model.MInOutLine;
import org.compiere.model.Query;
import org.compiere.util.DB;

import dev.vsuarez.preshipment.base.CustomProcess;
import dev.vsuarez.preshipment.model.MPreShipment;
import dev.vsuarez.preshipment.model.MPreShipmentLine;
import dev.vsuarez.preshipment.util.TimestampUtil;

/**
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 */
public class IVS_GeneratePreShipment extends CustomProcess {
	
	@Override
	protected void prepare() {
	}

	@Override
	protected String doIt() throws Exception {
		String sql = "SELECT T_Selection_ID, ViewID FROM T_Selection WHERE AD_PInstance_ID=? ORDER BY 2";
		List<List<Object>> selections = DB.getSQLArrayObjectsEx(get_TrxName(), sql, getAD_PInstance_ID());
		if(selections == null)
			return "No hay entregas seleccionadas";
		
		MPreShipment preShipment = null;
		int lineNo = 10;
		int count = 0;
		for(List<Object> selection : selections) {
			BigDecimal inOutLine = (BigDecimal) selection.get(0);
			MInOutLine ioLine = new MInOutLine(getCtx(), inOutLine.intValue(), get_TrxName());
			if(preShipment == null) {
				preShipment = new MPreShipment(getCtx(), 0, get_TrxName());
				preShipment.setAD_Org_ID(ioLine.getAD_Org_ID());
				preShipment.setAD_User_ID(getAD_User_ID());
				preShipment.setSalesRep_ID(getAD_User_ID());
				preShipment.setDateTrx(TimestampUtil.now());
				preShipment.setDateAcct(TimestampUtil.now());
				int C_DocType_ID = new Query(getCtx(), MDocType.Table_Name, "DocBaseType = 'PSM'", get_TrxName())
						.setClient_ID().setOnlyActiveRecords(true).setOrderBy("IsDefault DESC").firstId();
				preShipment.setC_DocType_ID(C_DocType_ID);
				preShipment.save();
			}
			MPreShipmentLine psLine = new MPreShipmentLine(getCtx(), 0, get_TrxName());
			psLine.setIVS_PreShipment_ID(preShipment.getIVS_PreShipment_ID());
			psLine.setAD_Org_ID(preShipment.getAD_Org_ID());
			psLine.setM_InOut_ID(ioLine.getM_InOut_ID());
			psLine.setM_InOutLine_ID(ioLine.getM_InOutLine_ID());
			psLine.setC_BPartner_ID(ioLine.getM_InOut().getC_BPartner_ID());
			psLine.setC_Order_ID(ioLine.getC_OrderLine().getC_Order_ID());
			psLine.setC_UOM_ID(ioLine.getC_UOM_ID());
			psLine.setLineNo(lineNo);
			psLine.setM_AttributeSetInstance_ID(ioLine.getM_AttributeSetInstance_ID());
			psLine.setM_Product_ID(ioLine.getM_Product_ID());
			psLine.setC_Charge_ID(ioLine.getC_Charge_ID());
			psLine.setM_Locator_ID(ioLine.getM_Locator_ID());
			psLine.setM_Warehouse_ID(ioLine.getM_InOut().getM_Warehouse_ID());
			psLine.setMovementQty(ioLine.getMovementQty());
			psLine.setQtyDispatched(ioLine.getMovementQty());
			psLine.save();
			lineNo+=10;
			count++;
		}
		addBufferLog(getProcessInfo().getAD_Process_ID(), preShipment.getDateTrx(), BigDecimal.ONE, "Pre-Despacho: " + preShipment.getDocumentNo(), MPreShipment.Table_ID, preShipment.getIVS_PreShipment_ID());
		return count + " Lineas Agregadas, en Pre-Despacho " + preShipment.getDocumentNo();
	}

}

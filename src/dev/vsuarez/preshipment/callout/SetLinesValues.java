/**
 * 
 */
package dev.vsuarez.preshipment.callout;

import org.compiere.model.MInOutLine;

import dev.vsuarez.preshipment.base.CustomCallout;
import dev.vsuarez.preshipment.model.MPreShipmentLine;

/**
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class SetLinesValues extends CustomCallout {

	@Override
	protected String start() {
		if(getValue() == null)
			return null;
		
		MInOutLine inOutLine = new MInOutLine(getCtx(), (int) getValue(), null);
		setValue(MPreShipmentLine.COLUMNNAME_C_BPartner_ID, inOutLine.getM_InOut().getC_BPartner_ID());
		setValue(MPreShipmentLine.COLUMNNAME_M_Product_ID, inOutLine.getM_Product_ID());
		setValue(MPreShipmentLine.COLUMNNAME_MovementQty, inOutLine.getMovementQty());
		setValue(MPreShipmentLine.COLUMNNAME_QtyDispatched, inOutLine.getMovementQty());
		setValue(MPreShipmentLine.COLUMNNAME_C_Charge_ID, inOutLine.getC_Charge_ID());
		setValue(MPreShipmentLine.COLUMNNAME_C_UOM_ID, inOutLine.getC_UOM_ID());
		setValue(MPreShipmentLine.COLUMNNAME_M_Warehouse_ID, inOutLine.getM_Warehouse_ID());
		setValue(MPreShipmentLine.COLUMNNAME_M_AttributeSetInstance_ID, inOutLine.getM_AttributeSetInstance_ID());
		setValue(MPreShipmentLine.COLUMNNAME_M_InOut_ID, inOutLine.getM_InOut_ID());
		setValue(MPreShipmentLine.COLUMNNAME_C_Order_ID, inOutLine.getC_OrderLine().getC_Order_ID());
		setValue(MPreShipmentLine.COLUMNNAME_M_Locator_ID, inOutLine.getM_Locator_ID());
		
		return null;
	}

}

/**
 * 
 */
package dev.vsuarez.preshipment.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class MPreShipmentLine extends X_IVS_PreShipmentLine {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7308157640043586959L;

	/**
	 * 
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public MPreShipmentLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	/**
	 * @param ctx
	 * @param IVS_PreShipmentLine_ID
	 * @param trxName
	 */
	public MPreShipmentLine(Properties ctx, int IVS_PreShipmentLine_ID, String trxName) {
		super(ctx, IVS_PreShipmentLine_ID, trxName);
	}

	@Override
	protected boolean beforeSave(boolean newRecord) {
		if(is_ValueChanged(COLUMNNAME_QtyDispatched)) {
			if(getMovementQty() != null && getQtyDispatched() != null) {
				if(getQtyDispatched().compareTo(getMovementQty()) > 0)
					setQtyDispatched(getMovementQty());
			}
		}
		return super.beforeSave(newRecord);
	}
	
	/**
	 * Get Weight Line
	 * @param qtyDispached
	 * @return
	 */
	protected BigDecimal getWeightLine(BigDecimal qtyDispatched) {
		BigDecimal weightProduct = getM_Product().getWeight();
		if(weightProduct == null)
			weightProduct = BigDecimal.ZERO;
		BigDecimal weightLine = qtyDispatched.multiply(weightProduct);
		return weightLine;
	}
	
	/**
	 * Get Total Line
	 * @param qtyDispached
	 * @return
	 */
	protected BigDecimal getTotalLine(BigDecimal qtyDispatched) {
		BigDecimal price = getM_InOutLine().getC_OrderLine().getPriceEntered();
		BigDecimal totalLine = price.multiply(qtyDispatched);
		return totalLine;
	}
	
}

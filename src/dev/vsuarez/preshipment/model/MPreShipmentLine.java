/**
 * 
 */
package dev.vsuarez.preshipment.model;

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
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ctx
	 * @param IVS_PreShipmentLine_ID
	 * @param trxName
	 */
	public MPreShipmentLine(Properties ctx, int IVS_PreShipmentLine_ID, String trxName) {
		super(ctx, IVS_PreShipmentLine_ID, trxName);
		// TODO Auto-generated constructor stub
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
	
	

}

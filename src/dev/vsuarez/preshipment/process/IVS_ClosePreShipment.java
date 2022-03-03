/**
 * 
 */
package dev.vsuarez.preshipment.process;

import java.sql.Timestamp;
import java.util.List;

import org.compiere.model.Query;

import dev.vsuarez.preshipment.base.CustomProcess;
import dev.vsuarez.preshipment.model.MPreShipment;

/**
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class IVS_ClosePreShipment extends CustomProcess {
	
	/**	Pre-Shipment	**/
	private int p_IVS_PreShipment_ID = 0;
	
	/**	Valid From	**/
	private Timestamp p_ValidFrom = null;
	
	/**	Valid To	**/
	private Timestamp p_ValidTo = null;
	
	/**	Organization	**/
	private int p_AD_Org_ID = 0;
	
	@Override
	protected void prepare() {
		Object parameter = getParameter("IVS_PreShipment_ID");
		if(parameter != null)
			p_IVS_PreShipment_ID = (int) parameter;
		parameter = getParameter("ValidFrom");
		if(parameter != null)
			p_ValidFrom = (Timestamp) parameter;
		parameter = getParameter("ValidTo");
		if(parameter != null)
			p_ValidTo = (Timestamp) parameter;
		parameter = getParameter("AD_Org_ID");
		if(parameter != null)
			p_AD_Org_ID = (int) parameter;
	}

	@Override
	protected String doIt() throws Exception {
		String whereClause = "DocStatus = 'CO' AND AD_Client_ID =? ";
		
		List<MPreShipment> preShipments = new Query(getCtx(), MPreShipment.Table_Name, whereClause, get_TrxName())
				.setOnlyActiveRecords(true).list();
		
		return null;
	}

}

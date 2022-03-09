/**
 * 
 */
package dev.vsuarez.preshipment.process;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
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
	private BigDecimal p_IVS_PreShipment_ID = BigDecimal.ZERO;
	
	/**	Valid From	**/
	private Timestamp p_ValidFrom = null;
	
	/**	Valid To	**/
	private Timestamp p_ValidTo = null;
	
	/**	Organization	**/
	private BigDecimal p_AD_Org_ID = BigDecimal.ZERO;
	
	@Override
	protected void prepare() {
		Object parameter = getParameter("IVS_PreShipment_ID");
		if((BigDecimal) parameter != null)
			p_IVS_PreShipment_ID = (BigDecimal) parameter;
		parameter = getParameter("ValidFrom");
		if(parameter != null)
			p_ValidFrom = (Timestamp) parameter;
		parameter = getParameter("ValidTo");
		if(parameter != null)
			p_ValidTo = (Timestamp) parameter;
		parameter = getParameter("AD_Org_ID");
		if((BigDecimal) parameter != null)
			p_AD_Org_ID = (BigDecimal) parameter;
	}

	@Override
	protected String doIt() throws Exception {
		StringBuilder whereClause = new StringBuilder(" DocStatus = 'CO' AND AD_Client_ID =? ");
		List<Object> parameters = new ArrayList<>();
		parameters.add(getAD_Client_ID());
		if(p_IVS_PreShipment_ID.signum() > 0) {
			whereClause.append(" AND IVS_PreShipment_ID =? ");
			parameters.add(p_IVS_PreShipment_ID);
		}
		if(p_AD_Org_ID.signum() > 0) {
			whereClause.append(" AND AD_Org_ID =? ");
			parameters.add(p_AD_Org_ID);
		}
		if(p_ValidFrom != null) {
			whereClause.append(" AND DateTrx >=? ");
			parameters.add(p_ValidFrom);
		}
		if(p_ValidTo != null) {
			whereClause.append(" AND DateTrx <=? ");
			parameters.add(p_ValidTo);
		}
		
		List<MPreShipment> preShipments = new Query(getCtx(), MPreShipment.Table_Name, whereClause.toString(), get_TrxName())
				.setOnlyActiveRecords(true).setParameters(parameters).list();
		
		int count = 0;
		for(MPreShipment preshipment : preShipments) {
			preshipment.setDocAction(MPreShipment.DOCACTION_Close);
			try {
				if(preshipment.processIt(MPreShipment.DOCACTION_Close)) {
					preshipment.save();
					count++;
				} else {
					preshipment.save();
					return "@Error@: No se Cerro Pre-Despacho: " + preshipment.getDocumentNo() + " - " + preshipment.getProcessMsg();
				}
			} catch (Exception e) {
				preshipment.save();
				return "@Error@: No se Cerro Pre-Despacho: " + preshipment.getDocumentNo() + " - " + preshipment.getProcessMsg() + " - " + e.getLocalizedMessage();
			}
		}
		
		return count + " Pre-Despachos Cerrados Correctamente";
	}

}

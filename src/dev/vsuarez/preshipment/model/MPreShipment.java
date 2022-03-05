package dev.vsuarez.preshipment.model;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.DocOptions;
import org.compiere.process.DocumentEngine;

/**
 * Model of Pre-Shipment
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class MPreShipment extends X_IVS_PreShipment implements DocAction, DocOptions {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8146930422226450983L;
	private String m_processMsg = null;

	public MPreShipment(Properties ctx, int IVS_PreShipment_ID, String trxName) {
		super(ctx, IVS_PreShipment_ID, trxName);
	}

	public MPreShipment(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	@Override
	public int customizeValidActions(String docStatus, Object processing, String orderType, String isSOTrx,
			int AD_Table_ID, String[] docAction, String[] options, int index) {
		if (options == null)
			throw new IllegalArgumentException("Option array parameter is null");
		if (docAction == null)
			throw new IllegalArgumentException("Doc action array parameter is null");

		// If a document is drafted or invalid, the users are able to complete, prepare or void
		if (docStatus.equals(DocumentEngine.STATUS_Drafted) || docStatus.equals(DocumentEngine.STATUS_Invalid)) {
			options[index++] = DocumentEngine.ACTION_Complete;
			options[index++] = DocumentEngine.ACTION_Prepare;
			options[index++] = DocumentEngine.ACTION_Void;

			// If the document is already completed, we also want to be able to reactivate or void it instead of only closing it
		} else if (docStatus.equals(DocumentEngine.STATUS_Completed)) {
			//options[index++] = DocumentEngine.ACTION_Void;
			options[index++] = DocumentEngine.ACTION_Close;
		}

		return index;
	}

	@Override
	public boolean processIt(String action) throws Exception {
		log.warning("Processing Action=" + action + " - DocStatus=" + getDocStatus() + " - DocAction=" + getDocAction());
		DocumentEngine engine = new DocumentEngine(this, getDocStatus());
		return engine.processIt(action, getDocAction());
	}

	@Override
	public boolean unlockIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean invalidateIt() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private List<MPreShipmentLine> m_lines = null;
	private String m_Summary = null;

	@Override
	public String prepareIt() {
		if (log.isLoggable(Level.INFO)) log.info(toString());
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		List<MPreShipmentLine> m_lines = getLines(null);
		if(m_lines != null) {
			for(MPreShipmentLine line : m_lines) {
				if((!MInOut.DOCSTATUS_Drafted.equalsIgnoreCase(line.getM_InOut().getDocStatus()) && !MInOut.DOCSTATUS_InProgress.equalsIgnoreCase(line.getM_InOut().getDocStatus())) 
						|| line.getQtyDispatched() == null || line.getQtyDispatched().signum() <=0 || line.getM_InOutLine_ID() <= 0)
					line.delete(true, null);
			}
		}
		m_lines = getLines(null);
		if(m_lines == null || m_lines.size() <= 0) {
			m_processMsg = "Pre-Despacho no tiene Lineas";
			return STATUS_Invalid;
		}
		
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		setIsApproved(true);
		return DocAction.STATUS_InProgress;
	}

	/**
	 * Get Lines
	 * @param whereClause
	 * @return Lines of Pre-Shipment
	 */
	private List<MPreShipmentLine> getLines(String whereClause) {
		if(whereClause == null)
			whereClause = "IVS_PreShipment_ID =?";
		List<MPreShipmentLine> lines = new Query(getCtx(), MPreShipmentLine.Table_Name, whereClause, get_TrxName())
				.setOnlyActiveRecords(true).setParameters(getIVS_PreShipment_ID())
				.setOrderBy("M_InOut_ID").list();
		return lines;
	}

	@Override
	public boolean approveIt() {
		if (log.isLoggable(Level.INFO)) log.info(toString());
		setIsApproved(true);
		return true;
	}

	@Override
	public boolean rejectIt() {
		if (log.isLoggable(Level.INFO)) log.info(toString());
		setIsApproved(false);
		return true;
	}
	
	/**	In Outs Generateds	**/
	StringBuilder m_inOutsGenerateds = new StringBuilder("");

	@Override
	public String completeIt() {
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		m_lines = getLines(null);
		if(m_lines == null || m_lines.size() <= 0) {
			m_processMsg = "Pre-Despacho no tiene Lineas";
			return STATUS_Invalid;
		}
		
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		List<MInOut> inOuts = setQty();
		cleanInOuts(inOuts);
		completeInOuts(inOuts);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		m_processMsg = m_Summary;
		setDescription(m_Summary);
		setShipmentsGenerated(m_inOutsGenerateds.toString());
		
		setProcessed(true);
		setDocAction(DOCACTION_Close);
		return DocAction.STATUS_Completed;
	}
	
	/**
	 * Complete M_InOuts of this Pre-Shipment
	 * @param inOuts - List of M_InOut to Complete
	 */
	private void completeInOuts(List<MInOut> inOuts) {
		int count = 0;
		StringBuilder msg = new StringBuilder("");
		for(MInOut io : inOuts) {
			try {
				io.setDocAction(MInOut.DOCACTION_Complete);
				if(io.processIt(MInOut.DOCACTION_Complete)) {
					io.save();
					msg.append(" ").append(io.getDocumentNo());
					if(m_inOutsGenerateds.length() > 0)
						m_inOutsGenerateds.append(",");
					m_inOutsGenerateds.append(io.getM_InOut_ID());
					count++;
				} else {
					io.save();
					m_processMsg = "No se Proceso la Entrega " + io.getDocumentNo() + " - " + io.getProcessMsg();
					return;
				}
			} catch (Exception e) {
				io.save();
				m_processMsg = "No se Proceso la Entrega " + io.getDocumentNo() + " - " + io.getProcessMsg() + " - " + e.getLocalizedMessage();
				return;
			}
		}
		m_Summary = count + " Entregas procesadas: " + msg.toString();
	}

	/**
	 * Delete Lines of Deliveries that will not be dispatched
	 * @param inOuts - List of M_InOut to Clean
	 */
	private void cleanInOuts(List<MInOut> inOuts) {
		for(MInOut ioDel : inOuts) {
			String whereClauseToDel = "M_InOut_ID =? AND C_Charge_ID IS NULL AND M_InOutLine_ID NOT IN "
					+ "(SELECT M_InOutLine_ID FROM IVS_PreShipmentLine WHERE IVS_PreShipment_ID =?)";
			List<MInOutLine> ioLinesToDel = new Query(getCtx(), MInOutLine.Table_Name, whereClauseToDel, get_TrxName())
					.setParameters(ioDel.getM_InOut_ID(), getIVS_PreShipment_ID()).list();
			for(MInOutLine iolDel : ioLinesToDel) {
				MPreShipmentLine psl = new Query(getCtx(), MPreShipmentLine.Table_Name, "M_InOutLine_ID =?", get_TrxName())
						.setOnlyActiveRecords(true).setParameters(iolDel.getM_InOutLine_ID()).first();
				if(psl != null) {
					psl.setM_InOutLine_ID(-1);
					psl.save();
				}
				iolDel.delete(true);
			}
		}
	}

	/**
	 * Set Movement Qty in M_InOutLine
	 * @return List of M_InOuts
	 */
	private List<MInOut> setQty() {
		List<MInOut> inOuts = new ArrayList<>();
		MInOut inOut = null;
		for(MPreShipmentLine line : m_lines) {
			if(inOut == null || inOut.getM_InOut_ID() != line.getM_InOut_ID()) {
				inOut = (MInOut) line.getM_InOut();
				inOuts.add(inOut);
			}
			MInOutLine ioLine = (MInOutLine) line.getM_InOutLine();
			ioLine.setMovementQty(line.getQtyDispatched());
			ioLine.setQtyEntered(line.getQtyDispatched());
			ioLine.save();
		}
		return inOuts;
	}

	@Override
	public boolean voidIt() {
		return true;
	}

	@Override
	public boolean closeIt() {
		if (log.isLoggable(Level.INFO)) log.info(toString());
		// Before Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_CLOSE);
		if (m_processMsg != null)
			return false;

		setProcessed(true);
		setDocAction(DOCACTION_None);

		// After Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_CLOSE);
		if (m_processMsg != null)
			return false;
		return true;
	}

	@Override
	public boolean reverseCorrectIt() {
		return false;
	}

	@Override
	public boolean reverseAccrualIt() {
		return false;
	}

	@Override
	public boolean reActivateIt() {
		return false;
	}

	@Override
	public String getSummary() {
		return m_Summary ;
	}

	@Override
	public String getDocumentInfo() {
		return getDocumentNo() + "_" + getDescription();
	}

	@Override
	public File createPDF() {
		return null;
	}

	@Override
	public String getProcessMsg() {
		return m_processMsg;
	}

	@Override
	public int getDoc_User_ID() {
		return getAD_User_ID();
	}

	@Override
	public int getC_Currency_ID() {
		return 0;
	}

	@Override
	public BigDecimal getApprovalAmt() {
		return null;
	}

}

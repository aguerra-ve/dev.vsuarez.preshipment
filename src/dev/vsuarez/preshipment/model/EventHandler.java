/**
 * 
 */
package dev.vsuarez.preshipment.model;

import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MOrderLine;
import org.compiere.model.PO;

import dev.vsuarez.preshipment.base.CustomEvent;

/**
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class EventHandler extends CustomEvent {

	@Override
	protected void doHandleEvent() {
		PO po = getPO();
		String topic = getEventType();
		
		if(po instanceof MOrderLine) {
			MOrderLine orderLine = (MOrderLine) po;
			if(!orderLine.getC_Order().isSOTrx())
				return;
			if(IEventTopics.PO_BEFORE_CHANGE.equals(topic)) {
				if(orderLine.is_ValueChanged(MOrderLine.COLUMNNAME_QtyDelivered) && orderLine.getQtyDelivered() != null) {
					if(orderLine.getQtyDelivered().compareTo(orderLine.getQtyOrdered()) > 0)
						throw new AdempiereException("Cantidad Ordenada: " + orderLine.getQtyOrdered() + ", ya fue entregada en su totalidad");
				}
			}
		}
	}

}

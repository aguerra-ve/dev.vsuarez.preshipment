/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright (C) 2022 Ing. Victor Suarez - victor.suarez.is@gmail.com and contributors (see README.md file).
 */

package dev.vsuarez.preshipment.component;

import dev.vsuarez.preshipment.base.CustomModelFactory;
import dev.vsuarez.preshipment.model.MPreShipment;
import dev.vsuarez.preshipment.model.MPreShipmentLine;

/**
 * Model Factory
 */
public class ModelFactory extends CustomModelFactory {

	/**
	 * For initialize class. Register the models to build
	 * 
	 * <pre>
	 * protected void initialize() {
	 * 	registerModel(MTableExample.Table_Name, MTableExample.class);
	 * }
	 * </pre>
	 */
	@Override
	protected void initialize() {
		registerModel(MPreShipment.Table_Name, MPreShipment.class);
		registerModel(MPreShipmentLine.Table_Name, MPreShipmentLine.class);
	}

}

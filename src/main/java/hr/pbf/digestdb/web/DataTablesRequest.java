/*
 * Copyright 2016 Eveoh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hr.pbf.digestdb.web;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;


import org.apache.commons.beanutils.BeanUtils;

import lombok.Data;

/**
 * @author Erik van Paassen
 *
 */
//@Data
public class DataTablesRequest {

	/**
	 * Draw counter. This is used by DataTables to ensure that the Ajax returns from
	 * server-side processing requests are drawn in sequence by DataTables (Ajax
	 * requests are asynchronous and thus can return out of sequence). This is used
	 * as part of the draw return parameter (see below).
	 */
	private int draw;

	/**
	 * Paging first record indicator. This is the start point in the current data
	 * set (0 index based - i.e. 0 is the first record).
	 *
	 */
	private long start;

	/**
	 * Number of records that the table can display in the current draw. It is
	 * expected that the number of records returned will be equal to this number,
	 * unless the server has fewer records to return. Note that this can be -1 to
	 * indicate that all records should be returned (although that negates any
	 * benefits of server-side processing!)
	 *
	 */
	private long length;

	private DataTablesSearch search;


	/**
	 * Ja dodao
	 */
	private Double massFrom;

	/**
	 * Ja dodao
	 */
	private Double massTo;

	/**
	 * Column's data source, as defined by columns.data
	 */
	private List<DataTablesColumn> columns = Collections.emptyList();

	private List<DataTablesOrder> order = Collections.emptyList();

	public List<DataTablesColumn> getColumns() {
		return columns;
	}

	public int getDraw() {
		return draw;
	}

	public long getLength() {
		return length;
	}

	public List<DataTablesOrder> getOrder() {
		return order;
	}

	public DataTablesSearch getSearch() {
		return search;
	}

	public void setColumns(List<DataTablesColumn> columns) {
		this.columns = columns;
	}

	public long getStart() {
		return start;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public void setLength(int length) {
		this.length = length;
	}


	public void setOrder(List<DataTablesOrder> order) {
		this.order = order;
	}

	public void setSearch(DataTablesSearch search) {
		this.search = search;
	}

	public void setStart(int start) {
		this.start = start;
	}


    public Double getMassFrom() {
        return massFrom;
    }

    public void setMassFrom(Double massFrom) {
        this.massFrom = massFrom;
    }

    public Double getMassTo() {
        return massTo;
    }

    public void setMassTo(Double massTo) {
        this.massTo = massTo;
    }
}

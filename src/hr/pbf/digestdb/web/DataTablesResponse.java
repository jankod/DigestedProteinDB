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

import java.util.Collections;
import java.util.List;

import lombok.Data;

//@Data
public class DataTablesResponse<T> {

	/**
	 * The draw counter that this object is a response to - from the draw parameter
	 * sent as part of the data request. Note that it is strongly recommended for
	 * security reasons that you cast this parameter to an integer, rather than
	 * simply echoing back to the client what it sent in the draw parameter, in
	 * order to prevent Cross Site Scripting (XSS) attacks.
	 * 
	 */
	int draw;

	/**
	 * Total records, before filtering (i.e. the total number of records in the
	 * database)
	 * 
	 */
	long recordsTotal;

	/**
	 * Total records, after filtering (i.e. the total number of records after
	 * filtering has been applied - not just the number of records being returned
	 * for this page of data).
	 */
	long recordsFiltered;

	/**
	 * The data to be displayed in the table. This is an array of data source
	 * objects, one for each row, which will be used by DataTables. Note that this
	 * parameter's name can be changed using the ajax option's dataSrc property.
	 * 
	 */
	List<T> data = Collections.emptyList();

	/**
	 * Optional: If an error occurs during the running of the server-side processing
	 * script, you can inform the user of this error by passing back the error
	 * message to be displayed using this parameter. Do not include if there is no
	 * error.
	 * 
	 */
	String error;

	public List<T> getData() {
		return data;
	}

	public int getDraw() {
		return draw;
	}

	public String getError() {
		return error;
	}

	public long getRecordsFiltered() {
		return recordsFiltered;
	}

	public long getRecordsTotal() {
		return recordsTotal;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setRecordsFiltered(long recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public void setRecordsTotal(long recordsTotal) {
		this.recordsTotal = recordsTotal;
	}
	
}
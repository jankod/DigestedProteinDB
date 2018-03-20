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

import lombok.Data;

/**
 * Column's data source, as defined by columns.data.
 * 
 * @author tag
 *
 */
//@Data
public class DataTablesColumn {

	/**
	 * Column's name, as defined by columns.name
	 */
	String name;


	/**
	 * Flag to indicate if this column is searchable (true) or not (false). This is
	 * controlled by columns.searchable.
	 * 
	 */
	boolean searchable;


	/**
	 * Flag to indicate if this column is orderable (true) or not (false). This is
	 * controlled by columns.orderable.
	 */
	boolean orderable;


	DataTablesSearch search = new DataTablesSearch();


	public String getName() {
		return name;
	}


	public DataTablesSearch getSearch() {
		return search;
	}


	public boolean isOrderable() {
		return orderable;
	}


	public boolean isSearchable() {
		return searchable;
	}


	public void setName(String name) {
		this.name = name;
	}
	
	public void setOrderable(boolean orderable) {
		this.orderable = orderable;
	}

	public void setSearch(DataTablesSearch search) {
		this.search = search;
	}
	
	
	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

}
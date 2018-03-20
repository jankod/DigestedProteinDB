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

//@Data
public class DataTablesSearch {

	/**
	 * Global search value. To be applied to all columns which have searchable as
	 * true.
	 */
	String value;
	/**
	 * true if the global filter should be treated as a regular expression for
	 * advanced searching, false otherwise. Note that normally server-side
	 * processing scripts will not perform regular expression searching for
	 * performance reasons on large data sets, but it is technically possible and at
	 * the discretion of your script.
	 * 
	 */
	boolean regex;
	public String getValue() {
		return value;
	}
	public boolean isRegex() {
		return regex;
	}
	public void setRegex(boolean regex) {
		this.regex = regex;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
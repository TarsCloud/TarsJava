/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.qq.tars.common.util;

import com.qq.tars.context.TarsContext;


public class DyeingSwitch {

	public static final String STATUS_DYED_KEY = "STATUS_DYED_KEY";
	public static final String STATUS_DYED_FILENAME = "STATUS_DYED_FILENAME";

	public static void enableActiveDyeing(final String servant) {
		TarsContext ctx = TarsContext.current();
		ctx.set(TarsContext.DYEING, true);
		ctx.set(TarsContext.DYEING_KEY, null);
		ctx.set(TarsContext.DYEING_FILENAME, servant == null ? "default" : servant);
	}

	public static void enableUnactiveDyeing(final String sDyeingKey, final String servant) {
		TarsContext ctx = TarsContext.current();
		ctx.set(TarsContext.DYEING, true);
		ctx.set(TarsContext.DYEING_KEY, sDyeingKey);
		ctx.set(TarsContext.DYEING_FILENAME, servant);
	}

	public static void closeActiveDyeing() {
		TarsContext ctx = TarsContext.current();
		ctx.set(TarsContext.DYEING, false);
		ctx.set(TarsContext.DYEING_KEY, null);
		ctx.set(TarsContext.DYEING_FILENAME, null);
	}

}

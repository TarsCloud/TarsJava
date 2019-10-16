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

package com.qq.tars.net.client.ticket;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeoutManager {

	protected static ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);

	public static class TimeoutTask implements  Runnable{

		Ticket<?> ticket;

		public TimeoutTask(Ticket<?> ticket) {
			this.ticket = ticket;
		}

		@Override
		public void run() {
			try {
				TicketManager.removeTicket(this.ticket.getTicketNumber());   //从ticketmanager中移除掉ticket
				this.ticket.expired();   //触发超时回调，通知业务方
			} catch(Exception e) {
				System.out.println("timeout exception:" + e);
			}
		}
	}
	/**
	 * 	添加定时任务到线程池执行
	 * @param task
	 * @param timeout ms
	 * @return
	 */
	public static Future<?> watch(Runnable task, long timeout) {
		return scheduled.schedule(task, timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * cancel超时的定时任务
	 * @param ticket
	 * @return
	 */
	public static boolean cancelTimeoutTask(Ticket<?> ticket) {
		return ticket.getTimeoutFuture().cancel(false);
	}

	public static void shutdown() {
		scheduled.shutdownNow();
	}


}

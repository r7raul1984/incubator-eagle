/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.eagle.alert.siddhi;

import org.apache.eagle.policy.siddhi.SiddhiPolicyEvaluator;
import org.junit.Assert;
import org.junit.Test;


public class TestSiddhiStream {
	
	@Test
	public void test() {
		String rule = "from hiveAccessLogStream[sensitivityType=='PHONE_NUMBER'] select user,timestamp,resource,define stream hdfsAuditLogEventStream(eagleAlertContext object, allowed string,cmd string,dst string,host string,securityZone string,sensitivityType string,src string,timestamp long,user string); @info(name = 'query') from hdfsAuditLogEventStream[cmd=='open'] select * insert into outputStream ; insert into outputStream;";
		Assert.assertFalse(SiddhiPolicyEvaluator.addContextFieldIfNotExist(rule).equals(rule));
		
		rule = "from hiveAccessLogStream[sensitivityType=='PHONE_NUMBER'] select    * insert into outputStream;";
		Assert.assertTrue(SiddhiPolicyEvaluator.addContextFieldIfNotExist(rule).equals(rule));
	}
}

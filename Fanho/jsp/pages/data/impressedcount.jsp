<%@ taglib prefix="s" uri="/struts-tags" %>
					<%-- 	<div id="msgimprcnt" ><s:property value="viewMessage.impressedCount"></s:property></div>
						<s:if test ="%{viewMessage.isCurrentUserImpressed==0}">
                    	
                    	<a class="impressedbtn" href="#" onclick="messageImpressed('#messageimpressedfrm<s:property value="viewMessage.messageId"></s:property>','<s:property value="viewMessage.messageId"></s:property>')" data-mini="true" data-inline="true" data-role="button" style="float:right;" title="Impressive">Wow&nbsp;+</a>
                    	</s:if>
                    	<s:else>
                    	<a class="unimpressedbtn" href="#" onclick="messageUnImpressed('#messageimpressedfrm<s:property value="viewMessage.messageId"></s:property>','<s:property value="viewMessage.messageId"></s:property>')" data-mini="true" data-inline="true" data-role="button" style="float:right;" title="Un Impressed">Wow&nbsp;-</a>
						</s:else> --%>
						
						
                   <span class="count"><s:property value="viewMessage.impressedCount"></s:property></span>
                   <s:if test ="%{viewMessage.isCurrentUserImpressed==0}">
                    	
                    	<a class="wow plus" href="#" onclick="messageImpressed('#messageimpressedfrm<s:property value="viewMessage.messageId"></s:property>','<s:property value="viewMessage.messageId"></s:property>')" data-mini="true" data-inline="true" data-role="button" title="Impressive">Wow&nbsp;+</a>
                    	</s:if>
                    	<s:else>
                    	<a class="wow minus" href="#" onclick="messageUnImpressed('#messageimpressedfrm<s:property value="viewMessage.messageId"></s:property>','<s:property value="viewMessage.messageId"></s:property>')" data-mini="true" data-inline="true" data-role="button" style="float:right;" title="Un Impressed">Wow&nbsp;-</a>
                    	</s:else>
                   
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
						
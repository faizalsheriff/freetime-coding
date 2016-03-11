<%@ taglib prefix="s" uri="/struts-tags" %>
       
         <div data-theme="c" data-role="header" data-position="fixed" style="display:inline">
          		<div data-role="navbar" data-theme="b" style="display:inline;margin-top:10px;border:blue;">
              	<a id="anchloadlistmsg" class="ui-btn-active" href="#" onclick="javascript:loadlistmessages()" style="margin-left:21%;
					background-image:-webkit-gradient(linear,left top,left bottom,from(#2a80a4),to(#414B4E));background-image:-webkit-linear-gradient( #2a80a4,#414B4E );background-image:-moz-linear-gradient(  #2a80a4,#414B4E );background-image:-ms-linear-gradient(  #2a80a4,#414B4E );background-image:-o-linear-gradient(  #2a80a4,#414B4E);background-image:linear-gradient( #f0f0f0,#ddd );border:white;">Messages</a>
                <a id="anchloadlistcmp" href="#" onclick="javascript:loadlistcompaigns()" style="margin-left:-4.5px;background-image:-webkit-gradient(linear,left top,left bottom,from(#2a80a4),to(#2cc4f2));background-image:-webkit-linear-gradient( #2a80a4,#2cc4f2 );background-image:-moz-linear-gradient(  #2a80a4,#2cc4f2 );background-image:-ms-linear-gradient(  #2a80a4,#2cc4f2 );background-image:-o-linear-gradient(  #2a80a4,#2cc4f2);background-image:linear-gradient( #f0f0f0,#ddd );color:#D6F3F8;border:blue;">Campaign</a>
               	</div> 
            		  <a href="#" style="margin-top:10px;" data-iconpos="notext" data-icon="menu" onclick="loadnavigation()">Menu</a>
            				
            		 <a href="#" onclick="loadaddmessage()" style="margin-top:10px;" data-transitions="none"  data-iconpos="notext" data-icon="adds" >Compose</a>
          		<!-- <a href="#popupPanel" data-rel="popup" data-transition="slide" data-position-to="window" data-role="button">Open panel</a> -->
         </div>
       
   		
   		
   		
        <div data-role="content" style="padding:2%;margin-top: 45px;">
        

         <s:if test="%{getMessageList().size()>0}">
         <s:iterator value="messageList" var="msgList">
            <div class="item">
               <div class="wrapper">
               <div class="content">
                    <div class="iteritem">
                    <div class="picholder">
                    <img src="img/pic_icon_32.png">
                    </div>
                       
                    <div class="nameholder">   
                        <a href="#"  class="name_title" onclick="javascript:viewuserdetails('<s:property value="#msgList.userName"></s:property>')"><s:property value="#msgList.userName"></s:property></a>
                       	
                    </div>
                    
                    <div id="messageimpressedfrm<s:property value="#msgList.messageId"></s:property>" class="otherholder">
                    
                    
                    	<div id="msgimprcnt" style="margin-top:1px;margin-left:-6px;float:right;border:5px;height:40px;width:auto;font-size: 14px;color: rgb(223, 63, 23);padding:1px;line;line-height: 0;"><s:property value="#msgList.impressedCount"></s:property></div>
                    	<s:if test ="%{#msgList.isCurrentUserImpressed==0}">
                    	
                    	<a class="impressedbtn" href="#" onclick="messageImpressed('#messageimpressedfrm<s:property value="#msgList.messageId"></s:property>','<s:property value="#msgList.messageId"></s:property>')" data-mini="true" data-inline="true" data-role="button" style="float:right;" title="Impressive">Wow&nbsp;+</a>
                    	</s:if>
                    	<s:else>
                    	<a class="unimpressedbtn" href="#" onclick="messageUnImpressed('#messageimpressedfrm<s:property value="#msgList.messageId"></s:property>','<s:property value="#msgList.messageId"></s:property>')" data-mini="true" data-inline="true" data-role="button" style="float:right;" title="Un Impressed">Wow&nbsp;-</a>
                    	</s:else>
                   
                    	
                    </div>
                        
                    <div class="previewholder">
                        <s:property value='#msgList.messagePreview'/>...
                    </div>
                    </div>
                    <!-- 
                    <div class="head_img">
                        <img src="img/NewsUpdates.jpg" class="item_img" />
                    </div>
                    -->
                    <s:form id="listmessageform%{#msgList.messageId}">
                    <s:hidden name = "userId" value="%{#msgList.userId}"></s:hidden>
                    <s:hidden name = "messageId" value="%{#msgList.messageId}"></s:hidden>
                    
                    
                    </s:form> 
              </div>
              </div>
                   <div class="bar">
                   			<a data-role="button" href="javascript:void(0)" onclick="return shareFB('http://www.rateurbeats.com/mywinks/viewpost?messageId=<s:property value="#msgList.messageId"/>','<s:property value="#msgList.userName"/> seeking you in My Winks','<s:property value="#msgList.messagePreview"/>','<s:property value="#msgList.userName"/> invites you to discuss on  <s:property value="#msgList.messagePreview"/>','')" data-icon="facebook" data-iconpos="notext">Share on Facebook</a>
							<a data-role="button" href="javascript:void(0)" onclick="return shareTwitter('http://www.rateurbeat.com/mywinks/viewpost?messageId=<s:property value="#msgList.messageId"/>','<s:property value="#msgList.messagePreview"/>')" data-icon="twitter" data-iconpos="notext" title="Share on Twitter">Share on Twitter</a>
							
                            <s:if test ="%{#msgList.messageCount>0}">
                           	<div style="margin-left:5px;margin-top:6px;"> <s:property value="#msgList.messageCount"></s:property> talking about this </div>
                            </s:if>
                            <a href="#" data-role="button" data-icon="post" class="btnGo" style="float:right;" data-iconpos="notext" onclick="loadMessageContents('#listmessageform<s:property value="#msgList.messageId"></s:property>')">Go</a>
                            
                   </div> 
            </div>
            </s:iterator>
         </s:if>
         
         <div id='idloadmoremessages<s:property value="pageCount"></s:property>'>
        
        </div>
        </div>
         <s:form id="loadmoremessagesform">
                    <s:hidden name = "offset" value="%{pageCount}"></s:hidden>
         </s:form> 
         <a id='idmasterloadmoremessages<s:property value="pageCount"></s:property>' href="#"  data-transitions="fade"  data-iconpos="notext" data-icon="adds" data-inline="true" onclick="javascript:loadmoremessages('#idmasterloadmoremessages<s:property value="pageCount"></s:property>','#idloadmoremessages<s:property value="pageCount"></s:property>','#loadmoremessagesform')">Load More Messages</a>
       
   <!--  </section>
       
</body>
</html>
 -->
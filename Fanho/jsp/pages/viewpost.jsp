<%@ taglib prefix="s" uri="/struts-tags" %>
       
         <div data-theme="c" data-role="header" data-position="fixed" >
         
               	<div class="menuicon left">
            			<a href="#" class="adjust_top" onclick="javascript:loadlistmessages()">
            				<div style="display:inline;float:left;margin:7px 3px;height:32px;width:32px;" class="menuicon ui-btn-corner-all"><img src="img/barrow_icon.png" style="margin-top:2px;"></div>
            			</a>
            	</div>	
           
          
          <!--   <a href="#" data-icon="like" data-iconpos="notext">Like</a> -->
        </div>
        <div data-role="content" style="height:100%; padding:0px; width:96%; margin:2%;">
        <div style="width:100%" class="post">
            <div class="head">
                <!-- <img class="pic" src="img/pic_icon.jpg" /> -->
                <div class="uinfo">
                    <p class="name"><s:property value="#sherrymode"></s:property></p>
                    <p class="time"><s:property value="viewMessage.lastUpdateTimeString"></s:property></p>
                </div>
            </div>
            <div class="post_content">
                <p class="posttxt">
                    <s:property value="viewMessage.messageContents"></s:property>
                </p>
            </div>
            
           
   
            
        </div>
     
        <s:if test = "%{viewMessage.mimepath!='' && viewMessage.mimepath!=null}">
        <div style="clear: both;"></div>
        <div id="spotlight" style="width:100%">
		
        <img src=" <s:url action='imagehandler?imageId=%{viewMessage.mimepath}' />"  class="myimgc"/>
        
		</div>  
        </s:if>
        
                <div class="comments" style="margin-top:7%;">
                <div class="likes">
                <s:if test="%{viewMessage.messageCount}>0">
                    <p class="liketxt">
                    
                     <s:property value="viewMessage.messageCount"></s:property> people like this
                   
                     </p>
                 </s:if>
                </div>
                  <s:form id="addCommentForm">
                 <%--    <s:hidden name = "struserId" value="%{loggedinuserId}"></s:hidden> --%>
                   <s:hidden name = "sid" value="1"></s:hidden>
                    <s:hidden name = "strmessageId" value="%{messageId}"></s:hidden>
                    <s:hidden id="idcommenthints" name = "commment.hints" value=""></s:hidden>
                    <s:hidden id="idcommentlastupdatetime" name = "commment.lastUpdateTimeString" value=""></s:hidden>
                    
            	</s:form> 
               
        	
                <ul class="comments_list">
                 <s:if test="%{viewMessage.messageHints!=null && viewMessage.getMessageHints().size()>0}">
                	<s:iterator value="viewMessage.messageHints" var="msgHints">
                    <li>
                        <!-- <img class="dp" src="img/pic_icon.jpg" /> -->
                        <div class="info">
                            <p class="name"><s:property value="#msgHints.userName"></s:property></p>
                            <p class="cmnt_txt"><s:property value="#msgHints.hints"></s:property></p>
                            <p class="infoad"><span class="time"><s:property value="#msgHints.lastUpdateTimeString"></s:property></span><!-- <a class="like_btn" href="#">Like</a> --></p>
                        </div>
                       <!-- <a href="#" class="del">x</a> -->
                    </li>
                    </s:iterator>
                     </s:if>
                     
                      <li class="cmnt_box">
                        <div class="txt">
                     
          			  <textarea id="txtStatus" placeholder="Compose Your Hints" class="txtareapost" name="txtStatus" ></textarea>
            			<a href="#" data-mini="true" class="btnConfirm" data-role="button" onclick="javascript:addComment('#addCommentForm')">Confirm</a>
                        </div>
                    </li>
                   
                </ul>
                
            </div>
        </div>
       
  
 
   
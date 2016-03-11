<%@ taglib prefix="s" uri="/struts-tags" %>
       
        <div data-theme="c" data-role="header" data-position="fixed" >
            <div class="menuicon left">
            			<a href="#" class="adjust_top" onclick="javascript:loadlistcompaigns()">
            				<div style="display:inline;float:left;margin:7px 3px;height:32px;width:32px;" class="menuicon ui-btn-corner-all"><img src="img/barrow_icon.png" style="margin-top:2px;"></div>
            			</a>
            </div>	
           
           
           
            
            <div class="menuicon right">
            			<a href="#" class="adjust_top" onclick="javascript:addCompaignJoin('#addCompaignJoinForm')">
            				<div style="display:inline;float:right;margin:7px 3px;height:32px;width:32px;" class="menuicon ui-btn-corner-all"><img src="img/voteup_icon.png" style="margin-top:0px;"></div>
            			</a>
            </div>	
          
            
        </div>
        <div data-role="content" style="height:100%; padding:0px; width:96%; margin:2%;">
        <div class="post">
            <div class="head">
                <!-- <img class="pic" src="img/pic_icon.jpg" /> -->
                <div class="uinfo">
                    <p class="name"><s:property value="loggedinuserId"></s:property></p>
                    <p class="time"><s:property value="viewCompaign.lastUpdateTimeString"></s:property></p>
                </div>
            </div>
            <div class="post_content">
                <p class="posttxt">
                    <s:property value="viewCompaign.compaignContents"></s:property>
                </p>
            </div>
              <s:form id="addCompaignJoinForm">
                 <%--    <s:hidden name = "struserId" value="%{loggedinuserId}"></s:hidden> --%>
                   <s:hidden name = "sid" value="1"></s:hidden>
                    <s:hidden name = "strcompaignId" value="%{viewCompaign.compaignId}"></s:hidden>
                    <s:hidden id="idjoinlastupdatetime" name = "join.lastUpdateTimeString" value=""></s:hidden>
                    
            </s:form> 
        </div>
            <div class="comments">
                <div class="likes">
                 <s:if test="%{viewCompaign.compaignCount}>0">
                    <p class="liketxt">
                   
                     <s:property value="viewCompaign.compaignCount"></s:property>joined this campaign
                    
                     </p>
                      </s:if>
                </div>
           
        	
                <ul class="comments_list">
                     <s:if test="%{viewCompaign.compaignHints!=null && viewCompaign.getCompaignHints().size()>0}">
                	<s:iterator value="viewCompaign.compaignHints" var="cmpHints">
                    <li>
                        <!-- <img class="dp" src="img/pic_icon.jpg" /> -->
                        <div class="info">
                            <p class="name"><s:property value="#cmpHints.userName"></s:property></p>
                            <p class="cmnt_txt"><s:property value="#cmpHints.hints"></s:property></p>
                            <p class="infoad"><span class="time"><s:property value="#cmpHints.lastUpdateTimeString"></s:property></span><!-- <a class="like_btn" href="#">Like</a> --></p>
                        </div>
                         <!-- <a href="#" class="del">x</a> -->
                    </li>
                    </s:iterator>
                     </s:if>
                       <li class="cmnt_box">
                        <div class="txt">
                         <s:form id="addCompaignCommentForm">
                 <%--    <s:hidden name = "struserId" value="%{loggedinuserId}"></s:hidden> --%>
                   <s:hidden name = "sid" value="1"></s:hidden>
                    <s:hidden name = "strcompaignId" value="%{compaignId}"></s:hidden>
                    <s:hidden id="idcommenthints" name = "commment.hints" value=""></s:hidden>
                    <s:hidden id="idcommentlastupdatetime" name = "commment.lastUpdateTimeString" value=""></s:hidden>
                    
            </s:form> 
            <textarea id="txtStatus" placeholder="Compose Your Hints.." class="txtareapost" name="txtStatus" ></textarea>
            <a href="#" data-mini="true" class="btnConfirm" data-role="button" onclick="javascript:addCompaignComment('#addCompaignCommentForm')">Confirm</a>
                        </div>
                        </li>
                   
                </ul>
                
            </div>
        </div>
       

 
   
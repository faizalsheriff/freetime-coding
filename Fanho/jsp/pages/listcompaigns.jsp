<%@ taglib prefix="s" uri="/struts-tags" %>
       
   		<div id="header" data-theme="c" class="custom_header" data-role="header" data-position="fixed" style="display:inline">
		<!-- <div class="menuicon left">
            			<a href="#" class="adjust_top" onclick="loadnavigation()">
            				<div style="display:inline;float:left;margin:7px 3px;height:32px;width:32px;" class="menuicon ui-btn-corner-all"><img src="img/latest_icon.png" style="margin-top:2px;"></div>
            			</a>
            	</div>	
            	
				
				<div class="menuicon right">	
            		 <a href="#" onclick="loadaddcompaigns()" class="adjust_top">
            		 	<div style="display:inline;float:right;margin:7px 3px;height:32px;width:42px;" class="menuicon ui-btn-corner-all"><div class="position_text">Add</div></div>
            		 </a>
            	</div>

		<div class="tabcontainer">
              	<a id="anchloadlistmsg" href="#" onclick="javascript:loadlistmessages()" style="text-decoration: none"> <div class="righttab">Messages</div></a>
                <a id="anchloadlistcmp" href="#" onclick="javascript:loadlistcompaigns()" style="text-decoration: none"><div class="lefttab">Campaign</div></a>
               	</div>  -->
		
            <a href="#" style="margin-top:35px;" onclick="loadnavigation()" data-rel="dialog" class="ui-btn-left"   data-transition="reverse slide" data-iconpos="notext" data-icon="menu-white">Menu</a>
            
            <div class="ui-title" role="heading" data-role="controlgroup" data-type="horizontal" aria-level="1">
                <a data-transitions="none"  onclick="javascript:loadlistmessages()"  data-role="button" href="#" data-mini="true" data-inline="true">Messages</a>
                <a data-transitions="none" href="#" onclick="javascript:loadlistcompaigns()" class="ui-btn-active " data-role="button" data-mini="true" data-inline="true">Campaign</a>
            </div>
            
            <a href="#" style="margin-top:35px;" class="ui-btn-right" data-transitions="pop"  data-iconpos="notext" data-icon="adds-white" onclick="loadaddcompaigns()">Compose</a>
          
        </div>
          <div data-role="content" style="padding:2%;margin-top: 45px;">
            <s:if test="%{getCompaignList().size()>0}">
            <s:iterator value="compaignList" var="cmpList">
            <%-- <div class="item">
               <div class="wrapper">
               <div class="content">
                  <div class="iteritem">
                    <div class="picholder">
                    <img src="img/pic_icon_32.png">
                    </div>
                    <div class="nameholder">   
                         <a href="#"  data-transitions="fade"  data-iconpos="notext" data-icon="adds" data-inline="true" onclick="javascript:viewuserdetails('<s:property value="#cmpList.userName"></s:property>')"><s:property value="#cmpList.userName"></s:property></a>
                    </div> 
		    

                        <div class="previewholder">
			<s:property value="#cmpList.compaignPreview"></s:property>....
			</div>
                    </div>
                    <div class="head_img">
                    
                      <!--   <img src="img/NewsUpdates.jpg" class="item_img" /> -->
                    </div>
                     <s:form id="listcompaignform%{#cmpList.compaignId}">
                    <s:hidden name = "struserId" value="%{#cmpList.userId}"></s:hidden>
                    <s:hidden name = "strcompaignId" value="%{#cmpList.compaignId}"></s:hidden>
                    
                    </s:form> 
              </div>
              </div>
                   <div class="bar">
                   			<a data-role="button" href="javascript:void(0)" onclick="return shareFB('http://www.rateurbeats.com/mywinks/viewcompaign?strcompaignId=<s:property value="#cmpList.compaignId"></s:property>','<s:property value="#cmpList.userName"/> inviting you for a Campaign in My Winks','Campaign is about  <s:property value="#cmpList.compaignPreview"/>','Your participation in this campaign can make a difference','')" data-icon="facebook" data-iconpos="notext">Share on Facebook</a>
							<a data-role="button" href="javascript:void(0)" onclick="return shareTwitter('http://www.rateurbeat.com/mywinks/viewcompaign','<s:property value="#cmpList.compaignPreview"/>')" data-icon="twitter" data-iconpos="notext">Share on Twitter</a>
                            
                         <!--    <a href="#" data-role="button" data-icon="like" data-iconpos="notext">Like</a> -->
                            <s:if test ="%{#cmpList.joinCount>0}">
                            <div style="margin-left:5px;margin-top:6px;">
                            <s:property value="#cmpList.joinCount"></s:property>Voted Up
                           </div>
                            </s:if>
                            
                            <s:if test ="%{#cmpList.comments>0}">
                            <s:property value="#cmpList.comments"></s:property> are talking about this campaign
                            </s:if>
                            
                            <!-- <a href="#" data-mini="true" class="btnConfirm" data-role="button">Confirm</a> -->
                            <a href="#" data-role="button" data-icon="post" class="btnGo" style="float:right;" data-iconpos="notext" onclick="loadCompaignContents('#listcompaignform<s:property value="#cmpList.compaignId"></s:property>')">Go</a>
                   </div>
                    
            </div> --%>
            
            <div class="item message">

               <div class="wrapper">
                   <div class="pad">
               <div class="content">
                    <div class="head_img">
                        <img src="img/pic_icon_32.png" />
					</div>
                    <div class="head_tittle">
            			<a href="#"  class="heading" onclick="javascript:viewuserdetails('<s:property value="#cmpList.userName"></s:property>')"><s:property value="#cmpList.userName"></s:property></a>						
                    </div>
                   
                   
					<div class="descrip"><s:property value="#cmpList.compaignPreview"></s:property>....</div>
                 
              </div>
                       </div>
					   <s:form id="listcompaignform%{#cmpList.compaignId}">
                    <s:hidden name = "struserId" value="%{#cmpList.userId}"></s:hidden>
                    <s:hidden name = "strcompaignId" value="%{#cmpList.compaignId}"></s:hidden>
                    
                    </s:form> 

              </div>
                   <div class="bar">
				   
						<a data-role="button" href="javascript:void(0)" onclick="return shareFB('http://www.rateurbeats.com/mywinks/viewcompaign?strcompaignId=<s:property value="#cmpList.compaignId"></s:property>','<s:property value="#cmpList.userName"/> inviting you for a Campaign in My Winks','Campaign is about  <s:property value="#cmpList.compaignPreview"/>','Your participation in this campaign can make a difference','')" data-icon="facebook" data-iconpos="notext">Share on Facebook</a>
							<a data-role="button" href="javascript:void(0)" onclick="return shareTwitter('http://www.rateurbeat.com/mywinks/viewcompaign','<s:property value="#cmpList.compaignPreview"/>')" data-icon="twitter" data-iconpos="notext">Share on Twitter</a>
                            
                        
                            <s:if test ="%{#cmpList.joinCount>0}">
                            <div style="margin-left:5px;margin-top:6px;">
                            <s:property value="#cmpList.joinCount"></s:property>Voted Up
                           </div>
                            </s:if>
                            
                            <s:if test ="%{#cmpList.comments>0}">
                            <s:property value="#cmpList.comments"></s:property> are talking about this campaign
                            </s:if>
                            
                            <!-- <a href="#" data-mini="true" class="btnConfirm" data-role="button">Confirm</a> -->
                            <a href="#" data-role="button" data-icon="post" class="btnGo" style="float:right;" data-iconpos="notext" onclick="loadCompaignContents('#listcompaignform<s:property value="#cmpList.compaignId"></s:property>')">Go</a>                                        
                   </div> 
            </div>
            
		</s:iterator>
		</s:if>
            <div id='idloadmorecompaigns<s:property value="pageCount"></s:property>'>
        
        </div>
         <s:form id="loadmorecompaignsform">
                    <s:hidden name = "offset" value="%{pageCount}"></s:hidden>
         </s:form> 
         <a id='idmasterloadmorecompaigns<s:property value="pageCount"></s:property>' href="#"  data-transitions="fade"  data-iconpos="notext" data-icon="adds" data-inline="true" onclick="javascript:loadmorecompaigns('#idmasterloadmorecompaigns<s:property value="pageCount"></s:property>','#idloadmorecompaigns<s:property value="pageCount"></s:property>','#loadmorecompaignsform')">Load More Compaigns</a>
        </div>
   
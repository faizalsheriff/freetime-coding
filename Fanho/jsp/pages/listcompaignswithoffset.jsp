<%@ page language="java"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

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
                        <p class="descrip"><s:property value="#cmpList.compaignPreview"></s:property>....</p>
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
                            <!-- <a href="#" data-role="button" data-icon="like" data-iconpos="notext">Like</a> -->
                            <a data-role="button" href="javascript:void(0)" onclick="return shareFB('http://www.rateurbeats.com/mywinks/viewcompaign?strcompaignId=<s:property value="#cmpList.compaignId"></s:property>','<s:property value="#cmpList.userName"/> inviting you for a Campaign in My Winks','Campaign is about  <s:property value="#cmpList.compaignPreview"/>','Your participation in this campaign can make a difference','')" data-icon="facebook" data-iconpos="notext">Share on Facebook</a>
							<a data-role="button" href="javascript:void(0)" onclick="return shareTwitter('http://www.rateurbeat.com/mywinks/viewcompaign','<s:property value="#cmpList.compaignPreview"/>')" data-icon="twitter" data-iconpos="notext">Share on Twitter</a>
                            <s:if test ="%{#cmpList.joinCount>0}">
                              <div style="margin-left:5px;margin-top:6px;">
                            <s:property value="#cmpList.joinCount"></s:property>Voted Up
                           </div>
                            </s:if>
                            
                            <s:if test ="%{#cmpList.comments>0}">
                            <p style="margin-top: 5px;margin-left: 15px;"> <s:property value="#cmpList.comments"></s:property> are talking about this compaign</p>
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
            			<a href="#"  data-transitions="fade"  data-iconpos="notext" data-icon="adds" data-inline="true" onclick="javascript:viewuserdetails('<s:property value="#cmpList.userName"></s:property>')"><s:property value="#cmpList.userName"></s:property></a>						
                    </div>
                   
                   
					<div class="descrip">
						<s:property value="#cmpList.compaignPreview"></s:property>....
					</div>
                 
              </div>
                       </div>
					     <s:form id="listcompaignform%{#cmpList.compaignId}">
                    <s:hidden name = "struserId" value="%{#cmpList.userId}"></s:hidden>
                    <s:hidden name = "strcompaignId" value="%{#cmpList.compaignId}"></s:hidden>
                    
                    </s:form> 

              </div>
                   <div class="bar">
				   
					   <!-- <a href="#" data-role="button" data-icon="like" data-iconpos="notext">Like</a> -->
                            <a data-role="button" href="javascript:void(0)" onclick="return shareFB('http://www.rateurbeats.com/mywinks/viewcompaign?strcompaignId=<s:property value="#cmpList.compaignId"></s:property>','<s:property value="#cmpList.userName"/> inviting you for a Campaign in My Winks','Campaign is about  <s:property value="#cmpList.compaignPreview"/>','Your participation in this campaign can make a difference','')" data-icon="facebook" data-iconpos="notext">Share on Facebook</a>
							<a data-role="button" href="javascript:void(0)" onclick="return shareTwitter('http://www.rateurbeat.com/mywinks/viewcompaign','<s:property value="#cmpList.compaignPreview"/>')" data-icon="twitter" data-iconpos="notext">Share on Twitter</a>
                            <s:if test ="%{#cmpList.joinCount>0}">
                              <div style="margin-left:5px;margin-top:6px;">
                            <s:property value="#cmpList.joinCount"></s:property>Voted Up
                           </div>
                            </s:if>
                            
                            <s:if test ="%{#cmpList.comments>0}">
                            <p style="margin-top: 5px;margin-left: 15px;"> <s:property value="#cmpList.comments"></s:property> are talking about this compaign</p>
                            </s:if>
                            
                            <!-- <a href="#" data-mini="true" class="btnConfirm" data-role="button">Confirm</a> -->
                            <a href="#" data-role="button" data-icon="post" class="btnGo" style="float:right;" data-iconpos="notext" onclick="loadCompaignContents('#listcompaignform<s:property value="#cmpList.compaignId"></s:property>')">Go</a>                                        
                   </div> 
            </div>
            
		</s:iterator>
		    <div id='idloadmorecompaigns<s:property value="pageCount"></s:property>'>
        
        </div>
         <s:form id="loadmorecompaignsform">
                    <s:hidden name = "offset" value="%{pageCount}"></s:hidden>
         </s:form> 
         <a id='idmasterloadmorecompaigns<s:property value="pageCount"></s:property>' href="#"  data-transitions="fade"  data-iconpos="notext" data-icon="adds" data-inline="true" onclick="javascript:loadmorecompaigns('#idmasterloadmorecompaigns<s:property value="pageCount"></s:property>','#idloadmorecompaigns<s:property value="pageCount"></s:property>','#loadmorecompaignsform')">Load More Messages</a>
   
		</s:if>
		 <s:else>
         No more records found
         </s:else>
        
   
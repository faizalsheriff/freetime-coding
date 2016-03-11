<%@ taglib prefix="s" uri="/struts-tags" %>
       
       	<div data-theme="c" data-role="header" data-position="fixed" >
            <%-- <a href="javascript:loadlistmessages()" data-transition="reverse slide" data-iconpos="notext" data-icon="backs">View User</a>
            <h1><s:property value="viewUser.userName"/></h1> --%>
                <div class="menuicon left">
            			<a href="#" class="adjust_top" onclick="javascript:loadlistmessages()">
            				<div style="display:inline;float:left;margin:7px 3px;height:32px;width:32px;" class="menuicon ui-btn-corner-all"><img src="img/barrow_icon.png" style="margin-top:2px;"></div>
            			</a>
            	</div>	
           
          <div class="headerbartextcontainer">
		  			<div class="headerbartext">View User</div>
		  </div>
		  
            <!-- <a href="#" data-iconpos="notext" data-icon="searchs">Search</a> -->
 		</div>
        <div data-role="content">
        <div class="me">
		<div class="info">
		<h1 class="name"><s:property value="viewUser.userName"></s:property></h1>
		<p>
		<s:if test="%{viewUser.isfollowedByYou == 0}">
		<a class="followbtn" href="#" onclick="addToWatchList('<s:property value="viewUser.userName"/>')" data-mini="true" data-inline="true" data-role="button" style="float:right;">follow </a>
		</s:if>
		<s:else>
		<a class="followingbtn" href="#" onclick="removeFromWatchList('<s:property value="viewUser.userName"/>')" data-mini="true" data-inline="true" data-role="button" style="float:right;">following</a>
		</s:else>
		</p>
	
		</div>
		<img class="pic" src="img/pic_icon.png"/>
		<div class="info_box">
		<p class="hd">What <s:property value="viewUser.userName"></s:property> will do as a president</p>
		<p><s:property value="viewUser.slogan"></s:property></p>
		
		</div>
		<div class="info_box">
		<p class="hd">My Interests</p>
		<p><s:property value="viewUser.aboutMe"></s:property></p>
		
		</div>
		</div>
		<div class="me" style="margin-top:10px;">

				<div class="ui-grid-b">
                    <div class="ui-block-a">
                    Following(<s:property value="viewUser.youAreWatching"/>) 
                    </div>
                    <div class="ui-block-b">
                    Followers (<s:property value="viewUser.watchedByYou"/>)
                    </div>
                  
                </div>
		<%-- <h3>Watching(<s:property value="viewUser.youAreWatching"/>) </h3>
		
	    <h3>Watched by (<s:property value="viewUser.watchedByYou"/>)</h3>
 --%>
	
                   <s:if test="%{viewUser.getWatchedByList().size()>0}">
                        <%-- <h3>
                            Watching
                        </h3>
                      
                        	  <s:iterator value="viewUser.watchedByList()" var="watchList">
                        	<div>
                        	 <div style="background-color: aqua;float:left">
                             <img src="img/pic_icon.jpg" />
                             </div>
                             <div style="background-color: white;float:left">
                              <a href="#"  data-transitions="fade"  data-iconpos="notext" data-icon="adds" data-inline="true" onclick="javascript:viewuserdetails('<s:property value="#watchList"/>')"><s:property value="#watchList"/></a>
                             </div>
                             </div>
                             
                             </s:iterator> --%>
                             
                             
                   <ul data-role="listview" data-divider-theme="b" data-inset="true">
                    <li data-role="list-divider" role="heading">
                        following (<s:property value="viewUser.youAreWatching"/>)
                    </li>
                    <s:iterator value="viewUser.watchedByList" var="watchList">
                    <li data-theme="c">
                       
                        <a href="#" data-transitions="fade"  onclick="javascript:viewuserdetails('<s:property value="#watchList"/>')"><s:property value="#watchList"/>
                           <div>
                           
                        	 <div style="background-color: aqua;float:left">
                             <img src="img/pic_icon.jpg" />
                             </div>
                            
                          </div>
                        </a>
                    </li>
                    </s:iterator>
                </ul>
                        </s:if>
                 
        
                    
	   <s:if test="%{viewUser.getWatchingYou().size()>0}">
		
						<%-- <h3>
                            Watched by
                        </h3>
                     
                        	  <s:iterator value="viewUser.watchingYou" var="watchingYou">
                        	 <div>
                        	 <div style="background-color: white;float:left">
                             <img src="img/pic_icon.jpg" />
                             </div>
                             <div style="background-color:white;padding:5px;">
                              <a href="#"  data-transitions="fade"  data-iconpos="notext" data-icon="adds" data-inline="true" onclick="javascript:viewuserdetails('<s:property value="#watchingYou"/>')"><s:property value="#watchingYou"/></a>
                             </div>
                             </div>
                             </s:iterator>
                        </s:if> --%>
                        
                          <ul data-role="listview" data-divider-theme="b" data-inset="true">
                    <li data-role="list-divider" role="heading">
                        followers (<s:property value="viewUser.watchedByYou"/>)
                    </li>
                    <s:iterator value="viewUser.watchingYou" var="watchingYou">
                    <li data-theme="c">
                       
                        <a href="#" data-transitions="fade"  onclick="javascript:viewuserdetails('<s:property value="#watchingYou"/>')"><s:property value="#watchingYou"/>
                           <div>
                           
                        	 <div style="background-color: aqua;float:left">
                             <img src="img/pic_icon.jpg" />
                             </div>
                            
                          </div>
                        </a>
                    </li>
                    </s:iterator>
                    
                </ul>
                </s:if>
                       
                        
                        
                        
			</div>						
		</div>

   


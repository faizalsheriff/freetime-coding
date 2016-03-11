<%@ taglib prefix="s" uri="/struts-tags" %>

            <s:if test="%{repListLength>0}">
            <s:iterator value="representatives.results" var="repList">
<%--             <div class="item">
               <div class="wrapper">
               <div class="content">
                    <div class="head_tittle">
                        <h1 class="heading"></h1> 
                          <s:if test ="%{#repList.party=='D'}">
                            <h2>Democratic </h2>
                          </s:if>
                          <s:elseif test ="%{#repList.party=='R'}">
                          	<h2>Republican</h2>
                          </s:elseif>
                          <s:else>Party is 
                          <s:property value="#repList.party"></s:property>
                          </s:else>
                      
                        <p class="descrip"><s:property value="#repList.office"></s:property>
                        </p>
                    </div>
                   
              </div>
              </div>
                   <div class="bar">
                            <a href="tel:+<s:property value='#repList.phone'/>" data-role="button" data-icon="call" data-iconpos="notext">Call</a>
                          	<a href="<s:property value='#repList.link'></s:property> data-role="button" data-icon="call" data-iconpos="notext">Call</a>
                            
                            
                   </div> 
            </div> --%>
            
            <div class="rep">
			<p class="name"><s:property value="#repList.name"></s:property></p>
			<p class="addr">Party -  <s:property value="#repList.party"></s:property></p>
			<p class="addr"><s:property value="#repList.office"></s:property></p>
			<a href="tel:<s:property value='#repList.phone'></s:property>" class="call btn"><span class="call_icon"></span>Call</a>
			<a href="<s:property value='#repList.link'></s:property>" class="website btn">Website</a>
			</div>
            
            
		</s:iterator>
		</s:if>
		<s:else>
		Please check your zip code or no members found.
		</s:else>
         
       
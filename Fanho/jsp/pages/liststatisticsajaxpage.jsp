<%@ taglib prefix="s" uri="/struts-tags" %>
	<table class="tb">
		<tbody>
		<tr>
		<th>Rank</th>
		</tr>
		<tr>
		<td><s:property value="statList.staterank"></s:property></td>
		</tr>
		<tr>
		<th>Senate</th>
		</tr>
		<tr>
		<td>
		<table>
		<thead><tr>
		<th>D</th>
		<th>R</th>
		<th>I</th>
		</tr></thead>
		<tbody>
		<tr>
		<td><s:property value="statList.senateD"></s:property></td>
		<td><s:property value="statList.senateR"></s:property></td>
		<td><s:property value="statList.senateI"></s:property></td>
		</tr>
		</tbody>
		</table>
		</td>
		</tr>
		<tr>
		<th>Total
Women/
Total
Senate</th>
		</tr>
		<tr>
		<td><s:property value="statList.totalWomenSenate"></s:property>/<s:property value="statList.totalSenate"></s:property></td>
		</tr>
		<tr>
		<th>House</th>
		</tr>
		<tr>
		<td>
		<table>
		<thead>
		<tr>
		<th>D</th>
		<th>R</th>
		<th>I</th>
		<th>Prg</th>
		</tr>
		
		</thead>
		<tbody>
		<tr>
		<td><s:property value="statList.houseD"></s:property></td>
		<td><s:property value="statList.houseR"></s:property></td>
		<td><s:property value="statList.houseI"></s:property></td>
		<td><s:property value="statList.housePrg"></s:property></td>
		</tr>
		</tbody>
		</table>
		</td>
		</tr>
		<tr>
		<th>Total
Women/
Total
House</th>
		</tr>
		<tr><td><s:property value="statList.totalWomenHouse"></s:property>/<s:property value="statList.totalHouse"></s:property></td></tr>
		<tr>
		<th>Total
Women/
Total
Legislature</th>
		</tr>
		<tr>
		<td><s:property value="statList.totalWomenLegis"></s:property>/<s:property value="statList.totalLegis"></s:property></td>
		</tr>
		<tr><th>%
Total
Women</th></tr>
<tr>
<td><s:property value="statList.percentTotalWomen"></s:property></td>
</tr>
		</tbody>
		</table>
		
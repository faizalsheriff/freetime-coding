FreeMarker Template example: ${message}  

=======================
===  County List   ====
=======================
<#list countries as c>
	${c_index + 1}. ${c}
</#list>
${name}
${following}
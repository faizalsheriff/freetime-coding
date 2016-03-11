<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<body>

<table>
    <thead>
    <td>First Name</td>
    <td>Last Name</td>
    </thead>
    <c:forEach var="person" items="${persons}">
        <tr>
            <td>${person.firstName}</td>
            <td>${person.lastName}</td>
        </tr>
    </c:forEach>
</table>

</body>
</html>
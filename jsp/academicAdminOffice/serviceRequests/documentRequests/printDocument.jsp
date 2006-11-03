<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>
<html:xhtml/>

<em><bean:message key="label.academicAdminOffice" bundle="ACADEMIC_OFFICE_RESOURCES"/></em>
<h2><bean:message bundle="ACADEMIC_OFFICE_RESOURCES" key="document.print" /></h2>

<hr style="margin-bottom: 2em;"/>

<strong><bean:message bundle="ACADEMIC_OFFICE_RESOURCES"  key="request.information"/></strong>
<bean:define id="academicServiceRequest" name="academicServiceRequest" scope="request" type="net.sourceforge.fenixedu.domain.serviceRequests.AcademicServiceRequest"/>
<bean:define id="simpleClassName" name="academicServiceRequest" property="class.simpleName" />
<fr:view name="academicServiceRequest" schema="<%= simpleClassName  + ".view"%>">
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle4" />
		<fr:property name="columnClasses" value="listClasses,," />
	</fr:layout>
</fr:view>

<br/><br/>

<logic:present name="academicServiceRequest" property="activeSituation">
	<strong><bean:message bundle="ACADEMIC_OFFICE_RESOURCES"  key="request.situation"/></strong>
	<fr:view name="academicServiceRequest" property="activeSituation" schema="AcademicServiceRequestSituation.view">
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle4" />
			<fr:property name="columnClasses" value="listClasses,," />
		</fr:layout>
	</fr:view>
</logic:present>

<logic:messagesNotPresent message="true">
	<p>
		<html:link page="/documentRequestsManagement.do?method=printDocument" paramId="documentRequestId" paramName="academicServiceRequest" paramProperty="idInternal">
			<bean:message key="print" bundle="APPLICATION_RESOURCES"/>
		</html:link>
	</p>
</logic:messagesNotPresent>
<html:messages id="message" message="true" bundle="ACADEMIC_OFFICE_RESOURCES">
	<p>
		<span class="warning0">
			<bean:write name="message" />
		</span>
	</p>
</html:messages>

<p>
	<strong><bean:message bundle="ACADEMIC_OFFICE_RESOURCES"  key="documentRequest.confirmDocumentSuccessfulPrinting"/></strong>
	<table>
		<tr>
			<th class="listClasses">
				<bean:message bundle="ACADEMIC_OFFICE_RESOURCES"  key="number.of.pages.printed"/>:
			</th>
			<td>
				<html:form action="<%="/academicServiceRequestsManagement.do?method=concludeAcademicServiceRequest&academicServiceRequestId=" + academicServiceRequest.getIdInternal().toString()%>">
				<html:text property="numberOfPages" size="3"/>
			</td>
		</tr>
		<tr>
			<td style="text-align: center;">
				<html:submit styleClass="inputbutton"><bean:message key="conclude" bundle="APPLICATION_RESOURCES"/></html:submit>
				</html:form>		
			</td>
			<td>
				<html:form action="<%="/student.do?method=visualizeRegistration&registrationID=" + academicServiceRequest.getRegistration().getIdInternal().toString()%>">
					<html:submit styleClass="inputbutton"><bean:message key="cancel" bundle="APPLICATION_RESOURCES"/></html:submit>
				</html:form>
			</td>
		</tr>
	</table>
</p>

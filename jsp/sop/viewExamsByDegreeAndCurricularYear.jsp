<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<tiles:insert page="/fenixLayout_2col.jsp" flush="true">
  <tiles:put name="title" value=".IST - SOP" />
  <tiles:put name="serviceName" value="SOP - Servi�o de Organiza��o Pedag�gica" />
  <tiles:put name="navGeral" value="/sop/commonNavGeralSopExam.jsp" />
  <tiles:put name="navLocal" value="/sop/commonNavLocalExamsSop.jsp" />
  <tiles:put name="body-context" value=""/>  
  <tiles:put name="body" value="/sop/viewExamsByDegreeAndCurricularYear_bd.jsp" />
  <tiles:put name="footer" value="/sop/commonFooterSop.jsp" />
  <tiles:put name="context" value="/commons/contextExecutionDegreeAndCurricularYear.jsp" />
</tiles:insert>

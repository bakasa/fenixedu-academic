package net.sourceforge.fenixedu.presentationTier.Action.publico;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.fenixedu.applicationTier.IUserView;
import net.sourceforge.fenixedu.dataTransferObject.InfoLesson;
import net.sourceforge.fenixedu.dataTransferObject.oldInquiries.StudentInquiriesCourseResultBean;
import net.sourceforge.fenixedu.dataTransferObject.oldInquiries.TeachingInquiryDTO;
import net.sourceforge.fenixedu.dataTransferObject.oldInquiries.YearDelegateCourseInquiryDTO;
import net.sourceforge.fenixedu.domain.Attends;
import net.sourceforge.fenixedu.domain.Coordinator;
import net.sourceforge.fenixedu.domain.Degree;
import net.sourceforge.fenixedu.domain.Evaluation;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.ExecutionCourseSite;
import net.sourceforge.fenixedu.domain.ExecutionDegree;
import net.sourceforge.fenixedu.domain.ExecutionSemester;
import net.sourceforge.fenixedu.domain.ExportGrouping;
import net.sourceforge.fenixedu.domain.Grouping;
import net.sourceforge.fenixedu.domain.Lesson;
import net.sourceforge.fenixedu.domain.LessonPlanning;
import net.sourceforge.fenixedu.domain.Mark;
import net.sourceforge.fenixedu.domain.Professorship;
import net.sourceforge.fenixedu.domain.RootDomainObject;
import net.sourceforge.fenixedu.domain.Shift;
import net.sourceforge.fenixedu.domain.ShiftType;
import net.sourceforge.fenixedu.domain.StudentGroup;
import net.sourceforge.fenixedu.domain.executionCourse.SummariesSearchBean;
import net.sourceforge.fenixedu.domain.functionalities.AbstractFunctionalityContext;
import net.sourceforge.fenixedu.domain.messaging.Announcement;
import net.sourceforge.fenixedu.domain.oldInquiries.InquiryResponsePeriod;
import net.sourceforge.fenixedu.domain.oldInquiries.StudentInquiriesCourseResult;
import net.sourceforge.fenixedu.domain.oldInquiries.StudentInquiriesTeachingResult;
import net.sourceforge.fenixedu.domain.oldInquiries.teacher.TeachingInquiry;
import net.sourceforge.fenixedu.domain.person.RoleType;
import net.sourceforge.fenixedu.domain.student.YearDelegateCourseInquiry;
import net.sourceforge.fenixedu.presentationTier.Action.manager.SiteVisualizationDA;
import net.sourceforge.fenixedu.util.PeriodState;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.util.RequestUtils;

import pt.ist.fenixWebFramework.security.UserView;


public class ExecutionCourseDA extends SiteVisualizationDA {

    public final static int ANNOUNCEMENTS_TO_SHOW = 5;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	final String executionCourseIDString = request.getParameter("executionCourseID");

	ExecutionCourse executionCourse = null;
	if (executionCourseIDString != null) {
	    executionCourse = rootDomainObject.readExecutionCourseByOID(Integer.valueOf(executionCourseIDString));
	} else {
	    ExecutionCourseSite site = (ExecutionCourseSite) AbstractFunctionalityContext.getCurrentContext(request)
		    .getSelectedContainer();
	    executionCourse = site.getSiteExecutionCourse();
	}

	request.setAttribute("executionCourse", executionCourse);
	request.setAttribute("executionCourseID", executionCourse.getIdInternal());

	return super.execute(mapping, actionForm, request, response);
    }

    @Override
    protected String getDirectLinkContext(HttpServletRequest request) {
	ExecutionCourse executionCourse = getExecutionCourse(request);
	try {
	    String path = executionCourse.getSite().getReversePath();

	    if (path == null) {
		return null;
	    }

	    return RequestUtils.absoluteURL(request, path).toString();
	} catch (MalformedURLException e) {
	    return null;
	}
    }

    protected ExecutionCourse getExecutionCourse(final HttpServletRequest request) {
	return (ExecutionCourse) request.getAttribute("executionCourse");
    }

    public ActionForward firstPage(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	final ExecutionCourse course = (ExecutionCourse) request.getAttribute("executionCourse");

	List<Announcement> activeAnnouncements = course.getBoard().getActiveAnnouncements();
	if (!activeAnnouncements.isEmpty()) {
	    Collections.sort(activeAnnouncements, Announcement.NEWEST_FIRST);
	}

	final Iterator<Announcement> announcementsIterator = activeAnnouncements.iterator();
	if (announcementsIterator.hasNext()) {
	    request.setAttribute("lastAnnouncement", announcementsIterator.next());
	}

	int i = 0;
	final Collection<Announcement> lastFiveAnnouncements = new ArrayList<Announcement>(ANNOUNCEMENTS_TO_SHOW);
	while (announcementsIterator.hasNext() && i < ANNOUNCEMENTS_TO_SHOW) {
	    lastFiveAnnouncements.add(announcementsIterator.next());
	    i++;
	}
	request.setAttribute("lastFiveAnnouncements", lastFiveAnnouncements);

	return mapping.findForward("execution-course-first-page");
    }

    public ActionForward summaries(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	final ExecutionCourse executionCourse = getExecutionCourse(request);
	final DynaActionForm dynaActionForm = (DynaActionForm) form;
	final SummariesSearchBean summariesSearchBean = executionCourse.getSummariesSearchBean();
	request.setAttribute("summariesSearchBean", summariesSearchBean);
	if (dynaActionForm != null) {
	    if (dynaActionForm.get("order").equals("ascendant")) {
		summariesSearchBean.setAscendant(true);
	    }
	    final String shiftType = (String) dynaActionForm.get("shiftType");
	    if (shiftType != null && shiftType.length() > 0) {
		summariesSearchBean.setShiftType(ShiftType.valueOf(shiftType));
	    }
	    final String shiftID = (String) dynaActionForm.get("shiftID");
	    if (shiftID != null && shiftID.length() > 0) {
		summariesSearchBean.setShift(rootDomainObject.readShiftByOID(Integer.valueOf(shiftID)));
	    }
	    final String professorshipID = (String) dynaActionForm.get("professorshipID");
	    if (professorshipID != null && professorshipID.equals("-1")) {
		summariesSearchBean.setShowOtherProfessors(Boolean.TRUE);
	    } else if (professorshipID != null && !professorshipID.equals("0")) {
		summariesSearchBean.setProfessorship(rootDomainObject.readProfessorshipByOID(Integer.valueOf(professorshipID)));
	    }
	}
	return mapping.findForward("execution-course-summaries");
    }

    public ActionForward objectives(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	return mapping.findForward("execution-course-objectives");
    }

    public ActionForward program(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	return mapping.findForward("execution-course-program");
    }

    public ActionForward evaluationMethod(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	return mapping.findForward("execution-course-evaluation-method");
    }

    public ActionForward bibliographicReference(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	return mapping.findForward("execution-course-bibliographic-reference");
    }

    public ActionForward lessonPlannings(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	final ExecutionCourse executionCourse = getExecutionCourse(request);
	Map<ShiftType, List<LessonPlanning>> lessonPlanningsMap = new TreeMap<ShiftType, List<LessonPlanning>>();
	if (executionCourse.getSite().getLessonPlanningAvailable()) {
	    for (ShiftType shiftType : executionCourse.getShiftTypes()) {
		List<LessonPlanning> lessonPlanningsOrderedByOrder = executionCourse.getLessonPlanningsOrderedByOrder(shiftType);
		if (!lessonPlanningsOrderedByOrder.isEmpty()) {
		    lessonPlanningsMap.put(shiftType, lessonPlanningsOrderedByOrder);
		}
	    }
	}
	request.setAttribute("lessonPlanningsMap", lessonPlanningsMap);
	return mapping.findForward("execution-course-lesson-plannings");
    }
    
    private boolean hasPermissionToViewSchedule(ExecutionCourse executionCourse) {
	if (executionCourse.getExecutionPeriod().getState() != PeriodState.NOT_OPEN) {
	    return true;
	}
	
	if (!UserView.hasUser()) { //public access
	    return false;
	}
	
	final IUserView userview =  (IUserView) UserView.getUser();
	if (userview.hasRoleType(RoleType.RESOURCE_ALLOCATION_MANAGER)) { // allow gop to view
	    return true;
	}
	
	for(Degree degree : executionCourse.getDegreesSortedByDegreeName()) {
	    for(Coordinator coordinator : degree.getCurrentCoordinators()) {
		if (coordinator.getPerson().equals(userview.getPerson())) {
		    return true;
		}
	    }
	}
	return false;
    }
    
    public ActionForward schedule(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	final ExecutionCourse executionCourse = getExecutionCourse(request);
	if (hasPermissionToViewSchedule(executionCourse)) {
	    final List<InfoLesson> infoLessons = new ArrayList<InfoLesson>();
	    for (final Lesson lesson : executionCourse.getLessons()) {
		infoLessons.add(InfoLesson.newInfoFromDomain(lesson));
	    }
	    request.setAttribute("infoLessons", infoLessons);
	}
	return mapping.findForward("execution-course-schedule");
    }

    public ActionForward shifts(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	return mapping.findForward("execution-course-shifts");
    }

    public ActionForward evaluations(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	return mapping.findForward("execution-course-evaluations");
    }

    public ActionForward marks(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	final ExecutionCourse executionCourse = getExecutionCourse(request);
	final Map<Attends, Map<Evaluation, Mark>> attendsMap = new TreeMap<Attends, Map<Evaluation, Mark>>(
		Attends.COMPARATOR_BY_STUDENT_NUMBER);
	for (final Attends attends : executionCourse.getAttendsSet()) {
	    final Map<Evaluation, Mark> evaluationsMap = new TreeMap<Evaluation, Mark>(ExecutionCourse.EVALUATION_COMPARATOR);
	    attendsMap.put(attends, evaluationsMap);
	    for (final Evaluation evaluation : executionCourse.getAssociatedEvaluationsSet()) {
		if (evaluation.getPublishmentMessage() != null) {
		    evaluationsMap.put(evaluation, null);
		}
	    }
	    for (final Mark mark : attends.getAssociatedMarksSet()) {
		if (mark.getEvaluation().getPublishmentMessage() != null) {
		    evaluationsMap.put(mark.getEvaluation(), mark);
		}
	    }
	}
	request.setAttribute("attendsMap", attendsMap);
	request.setAttribute("dont-cache-pages-in-search-engines", Boolean.TRUE);
	return mapping.findForward("execution-course-marks");
    }

    public ActionForward groupings(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	return mapping.findForward("execution-course-groupings");
    }

    public ActionForward grouping(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	final Grouping grouping = getGrouping(request);
	request.setAttribute("grouping", grouping);
	return mapping.findForward("execution-course-grouping");
    }

    public ActionForward studentGroup(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	final StudentGroup studentGroup = getStudentGroup(request);
	request.setAttribute("studentGroup", studentGroup);
	return mapping.findForward("execution-course-student-group");
    }

    public ActionForward studentGroupsByShift(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	final Grouping grouping = getGrouping(request);
	request.setAttribute("grouping", grouping);
	final Shift shift = getShift(request);
	if (shift != null) {
	    request.setAttribute("shift", shift);
	}
	final List<StudentGroup> studentGroups = shift == null ? grouping.getStudentGroupsWithoutShift() : grouping
		.readAllStudentGroupsBy(shift);
	Collections.sort(studentGroups, StudentGroup.COMPARATOR_BY_GROUP_NUMBER);
	request.setAttribute("studentGroups", studentGroups);
	return mapping.findForward("execution-course-student-groups-by-shift");
    }

    public ActionForward rss(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	return mapping.findForward("execution-course-rss");
    }

    protected StudentGroup getStudentGroup(final HttpServletRequest request) {
	final Integer studentGroupID = Integer.valueOf(request.getParameter("studentGroupID"));
	return rootDomainObject.readStudentGroupByOID(studentGroupID);
    }

    protected Shift getShift(final HttpServletRequest request) {
	if (request.getParameter("shiftID") != null) {
	    final Integer shiftID = Integer.valueOf(request.getParameter("shiftID"));
	    return rootDomainObject.readShiftByOID(shiftID);
	} else {
	    return null;
	}
    }

    protected Grouping getGrouping(final HttpServletRequest request) {
	final Integer groupingID = Integer.valueOf(request.getParameter("groupingID"));
	final ExecutionCourse executionCourse = getExecutionCourse(request);
	for (final ExportGrouping exportGrouping : executionCourse.getExportGroupingsSet()) {
	    final Grouping grouping = exportGrouping.getGrouping();
	    if (grouping.getIdInternal().equals(groupingID)) {
		return grouping;
	    }
	}
	return null;
    }

    public ActionForward notFound(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	return mapping.findForward("execution-course-not-found");
    }

    protected ActionForward getSiteDefaultView(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	return mapping.findForward("execution-course-first-page");
    }

    public ActionForward studentInquiriesResults(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {

	ExecutionCourse executionCourse = getExecutionCourse(request);
	ExecutionSemester executionPeriod = executionCourse.getExecutionPeriod();
	InquiryResponsePeriod inquiryResponsePeriod = executionPeriod.getInquiryResponsePeriod();

	return dispatchToInquiriesResultPage(mapping, request, executionCourse, executionPeriod, inquiryResponsePeriod);
    }

    private ActionForward dispatchToInquiriesResultPage(ActionMapping mapping, HttpServletRequest request,
	    ExecutionCourse executionCourse, ExecutionSemester executionPeriod, InquiryResponsePeriod inquiryResponsePeriod) {

	if (inquiryResponsePeriod == null || inquiryResponsePeriod.isAfterNow()) {
	    // msg -1
	    request.setAttribute("notAvailableMessage", "message.inquiries.publicResults.notAvailable.m1");
	    return mapping.findForward("execution-course-student-inquiries-result-notAvailable");
	}

	if (executionPeriod.isBefore(ExecutionSemester.readBySemesterAndExecutionYear(2, "2007/2008"))) {
	    // msg -2
	    request.setAttribute("notAvailableMessage", "message.inquiries.publicResults.notAvailable.m2");
	    return mapping.findForward("execution-course-student-inquiries-result-notAvailable");
	}

	if (!executionCourse.getAvailableForInquiries()) {
	    // msg NA
	    request.setAttribute("notAvailableMessage", "message.inquiries.publicResults.notAvailable.na");
	    return mapping.findForward("execution-course-student-inquiries-result-notAvailable");
	}

	if (inquiryResponsePeriod.isOpen()) {
	    // msg 0
	    request.setAttribute("notAvailableMessage", "message.inquiries.publicResults.notAvailable.0");
	    return mapping.findForward("execution-course-student-inquiries-result-notAvailable");
	}

	if (inquiryResponsePeriod.isBeforeNow()) {
	    InquiryResponsePeriod teachingInquiryResponsePeriod = executionPeriod.getTeachingInquiryResponsePeriod();

	    if (teachingInquiryResponsePeriod != null) {
		if (teachingInquiryResponsePeriod.getBegin().plusDays(15).isAfterNow()) {
		    // msg 1
		    request.setAttribute("notAvailableMessage", "message.inquiries.publicResults.notAvailable.1");
		    return mapping.findForward("execution-course-student-inquiries-result-notAvailable");
		} else if (teachingInquiryResponsePeriod.isOpen()) {
		    // msg 2
		    final Collection<StudentInquiriesCourseResultBean> studentInquiriesCourseResults = populateStudentInquiriesCourseResults(getExecutionCourse(request));
		    if (studentInquiriesCourseResults.isEmpty()) {
			request.setAttribute("notAvailableMessage", "message.inquiries.publicResults.notAvailable.2sr");
			return mapping.findForward("execution-course-student-inquiries-result-notAvailable");
		    } else {
			request.setAttribute("studentInquiriesCourseResults", studentInquiriesCourseResults);
			return mapping.findForward("execution-course-student-inquiries-result");
		    }
		} else if (teachingInquiryResponsePeriod.isBeforeNow()) {
		    // msg 3
		    request.setAttribute("studentInquiriesCourseResults",
			    populateStudentInquiriesCourseFullResults(getExecutionCourse(request)));
		    return mapping.findForward("execution-course-student-inquiries-full-result");
		}
	    }
	}

	return mapping.findForward("execution-course-student-inquiries-result");
    }

    public ActionForward showInquiryCourseResult(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	final StudentInquiriesCourseResult courseResult = RootDomainObject.getInstance().readStudentInquiriesCourseResultByOID(
		getIntegerFromRequest(request, "resultId"));
	request.setAttribute("inquiryResult", courseResult);

	final ExecutionSemester executionPeriod = courseResult.getExecutionCourse().getExecutionPeriod();
	if (executionPeriod.getSemester() == 2 && executionPeriod.getYear().equals("2007/2008")) {
	    return actionMapping.findForward("execution-course-show-course-inquiries-result");
	}

	request.setAttribute("publicContext", true);
	return new ActionForward(null, "/inquiries/showCourseInquiryResult_v2.jsp", false, "/teacher");
    }

    public ActionForward showInquiryTeachingResult(ActionMapping actionMapping, ActionForm actionForm,
	    HttpServletRequest request, HttpServletResponse response) throws Exception {
	final StudentInquiriesTeachingResult teachingResult = RootDomainObject.getInstance()
		.readStudentInquiriesTeachingResultByOID(getIntegerFromRequest(request, "resultId"));
	request.setAttribute("inquiryResult", teachingResult);

	final ExecutionSemester executionPeriod = teachingResult.getProfessorship().getExecutionCourse().getExecutionPeriod();
	if (executionPeriod.getSemester() == 2 && executionPeriod.getYear().equals("2007/2008")) {
	    return actionMapping.findForward("execution-course-show-teaching-inquiries-result");
	}

	request.setAttribute("publicContext", true);
	return new ActionForward(null, "/inquiries/showTeachingInquiryResult_v2.jsp", false, "/teacher");
    }

    public ActionForward showInquiryTeachingReport(ActionMapping actionMapping, ActionForm actionForm,
	    HttpServletRequest request, HttpServletResponse response) {

	final TeachingInquiry teachingInquiry = RootDomainObject.getInstance().readTeachingInquiryByOID(
		getIntegerFromRequest(request, "teachingInquiry"));
	request.setAttribute("teachingInquiry", teachingInquiry);

	final ExecutionSemester executionPeriod = teachingInquiry.getProfessorship().getExecutionCourse().getExecutionPeriod();
	if (executionPeriod.getSemester() == 2 && executionPeriod.getYear().equals("2007/2008")) {
	    return new ActionForward(null, "/inquiries/showFilledTeachingInquiry.jsp", false, "/coordinator");
	}

	request.setAttribute("teachingInquiryDTO", new TeachingInquiryDTO(teachingInquiry.getProfessorship()));
	return new ActionForward(null, "/inquiries/showFilledTeachingInquiry_v2.jsp", false, "/coordinator");

    }

    public ActionForward showYearDelegateInquiryReport(ActionMapping actionMapping, ActionForm actionForm,
	    HttpServletRequest request, HttpServletResponse response) throws Exception {

	final YearDelegateCourseInquiry delegateCourseInquiry = RootDomainObject.getInstance()
		.readYearDelegateCourseInquiryByOID(getIntegerFromRequest(request, "yearDelegateInquiryId"));

	request.setAttribute("delegateInquiryDTO", new YearDelegateCourseInquiryDTO(delegateCourseInquiry));
	return new ActionForward(null, "/inquiries/showFilledDelegateInquiry.jsp", false, "/coordinator");
    }

    private Collection<StudentInquiriesCourseResultBean> populateStudentInquiriesCourseFullResults(
	    final ExecutionCourse executionCourse) {
	Map<ExecutionDegree, StudentInquiriesCourseResultBean> courseResultsMap = new HashMap<ExecutionDegree, StudentInquiriesCourseResultBean>();
	for (StudentInquiriesCourseResult studentInquiriesCourseResult : executionCourse.getStudentInquiriesCourseResults()) {
	    boolean publicDisclosure = studentInquiriesCourseResult.getPublicDisclosure() != null
		    && studentInquiriesCourseResult.getPublicDisclosure();
	    final TeachingInquiry responsibleTeachingInquiry = studentInquiriesCourseResult.getExecutionCourse()
		    .getResponsibleTeachingInquiry();
	    if (publicDisclosure
		    || (responsibleTeachingInquiry != null && responsibleTeachingInquiry.getResultsDisclosureToAcademicComunity())) {
		courseResultsMap.put(studentInquiriesCourseResult.getExecutionDegree(), new StudentInquiriesCourseResultBean(
			studentInquiriesCourseResult));
	    }
	}

	for (Professorship otherTeacherProfessorship : executionCourse.getProfessorships()) {
	    for (StudentInquiriesTeachingResult studentInquiriesTeachingResult : otherTeacherProfessorship
		    .getStudentInquiriesTeachingResults()) {
		final boolean publicDisclosure = studentInquiriesTeachingResult.getPublicDegreeDisclosure() != null
			&& studentInquiriesTeachingResult.getPublicDegreeDisclosure();
		final TeachingInquiry teachingInquiry = studentInquiriesTeachingResult.getProfessorship().getTeachingInquiry();
		if (publicDisclosure || (teachingInquiry != null && teachingInquiry.getResultsDisclosureToAcademicComunity())) {
		    final StudentInquiriesCourseResultBean studentInquiriesCourseResultBean = courseResultsMap
			    .get(studentInquiriesTeachingResult.getExecutionDegree());
		    if (studentInquiriesCourseResultBean != null) {
			studentInquiriesCourseResultBean.addStudentInquiriesTeachingResult(studentInquiriesTeachingResult);
		    }
		}
	    }
	}
	return courseResultsMap.values();
    }

    private Collection<StudentInquiriesCourseResultBean> populateStudentInquiriesCourseResults(
	    final ExecutionCourse executionCourse) {
	Map<ExecutionDegree, StudentInquiriesCourseResultBean> courseResultsMap = new HashMap<ExecutionDegree, StudentInquiriesCourseResultBean>();
	for (StudentInquiriesCourseResult studentInquiriesCourseResult : executionCourse.getStudentInquiriesCourseResults()) {
	    if (studentInquiriesCourseResult.getPublicDisclosure() != null && studentInquiriesCourseResult.getPublicDisclosure()) {
		courseResultsMap.put(studentInquiriesCourseResult.getExecutionDegree(), new StudentInquiriesCourseResultBean(
			studentInquiriesCourseResult));
	    }
	}

	for (Professorship otherTeacherProfessorship : executionCourse.getProfessorships()) {
	    for (StudentInquiriesTeachingResult studentInquiriesTeachingResult : otherTeacherProfessorship
		    .getStudentInquiriesTeachingResults()) {
		if (studentInquiriesTeachingResult.getPublicDegreeDisclosure() != null
			&& studentInquiriesTeachingResult.getPublicDegreeDisclosure()) {
		    final StudentInquiriesCourseResultBean studentInquiriesCourseResultBean = courseResultsMap
			    .get(studentInquiriesTeachingResult.getExecutionDegree());
		    if (studentInquiriesCourseResultBean != null) {
			studentInquiriesCourseResultBean.addStudentInquiriesTeachingResult(studentInquiriesTeachingResult);
		    }
		}
	    }
	}
	return courseResultsMap.values();
    }

}
/*
 * Created on Oct 7, 2004
 */
package ServidorApresentacao.Action.commons.curriculumHistoric;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.util.LabelValueBean;

import DataBeans.InfoCurricularCourse;
import DataBeans.InfoCurricularCourseScope;
import DataBeans.InfoCurricularYear;
import DataBeans.InfoDegreeCurricularPlan;
import DataBeans.InfoDegreeCurricularPlanWithCurricularCourseScopes;
import ServidorAplicacao.IUserView;
import ServidorAplicacao.Servico.exceptions.FenixServiceException;
import ServidorApresentacao.Action.base.FenixDispatchAction;
import ServidorApresentacao.Action.exceptions.FenixActionException;
import ServidorApresentacao.Action.sop.utils.ServiceUtils;
import ServidorApresentacao.Action.sop.utils.SessionUtils;

/**
 * @author nmgo
 * @author lmre
 */
public class DegreeCurricularPlanExecutionYearDispacthAction extends FenixDispatchAction {

    public ActionForward prepare(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws FenixActionException {
        IUserView userView = SessionUtils.getUserView(request);
        List executionYears = null;
        try {
            executionYears = (List) ServiceUtils.executeService(userView, "ReadNotClosedExecutionYears",
                    new Object[] {});
        } catch (FenixServiceException e) {
            throw new FenixActionException(e);
        }
        request.setAttribute("executionYears", executionYears);
        request.setAttribute("degreeCurricularPlans", null);
        return mapping.findForward("chooseExecutionYear");
    }

    public ActionForward chooseDegreeCurricularPlan(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws FenixActionException {
        DynaActionForm actionForm = (DynaActionForm) form;
        String executionYearID = (String) actionForm.get("executionYearID");
        IUserView userView = SessionUtils.getUserView(request);
        List executionYears = null;
        List degreeCurricularPlans = null;

        try {
            executionYears = (List) ServiceUtils.executeService(userView, "ReadNotClosedExecutionYears",
                    new Object[] {});
            if (executionYearID != null && executionYearID.length() > 0) {
                Object[] args = new Object[] { new Integer(executionYearID) };
                degreeCurricularPlans = (List) ServiceUtils.executeService(userView,
                        "ReadActiveDegreeCurricularPlansByExecutionYear", args);
            }
        } catch (FenixServiceException e) {
            throw new FenixActionException(e);
        }
        
        ComparatorChain comparatorChain = new ComparatorChain();
        comparatorChain.addComparator(new BeanComparator("infoDegree.tipoCurso.tipoCurso"));
        comparatorChain.addComparator(new BeanComparator("infoDegree.nome"));
        comparatorChain.addComparator(new BeanComparator("name"));
        Collections.sort(degreeCurricularPlans, comparatorChain);
        
        
        List labelValueDegreeCurricularPlans = (List) CollectionUtils.collect(degreeCurricularPlans, new Transformer() {
           public Object transform(Object obj) {
               InfoDegreeCurricularPlan infoDegreeCurricularPlan = (InfoDegreeCurricularPlan) obj;
               LabelValueBean valueBean = new LabelValueBean(infoDegreeCurricularPlan.getInfoDegree().getNome() + " - " + infoDegreeCurricularPlan.getName(), infoDegreeCurricularPlan.getIdInternal().toString());
               return valueBean;
           }
        });
        
        
        request.setAttribute("degreeCurricularPlans", labelValueDegreeCurricularPlans);
        request.setAttribute("executionYears", executionYears);
        return mapping.findForward("chooseExecutionYear");
    }

    public ActionForward showActiveCurricularCourseScope(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws FenixActionException {

        IUserView userView = SessionUtils.getUserView(request);
        DynaActionForm actionForm = (DynaActionForm) form;
        String executionYearID = (String) actionForm.get("executionYearID");
        String degreeCurricularPlanID = (String) actionForm.get("degreeCurricularPlanID");
        Object[] args2 = { new Integer(degreeCurricularPlanID), new Integer(executionYearID) };

        InfoDegreeCurricularPlanWithCurricularCourseScopes result = null;
        ActionErrors errors = new ActionErrors();
        try {
            result = (InfoDegreeCurricularPlanWithCurricularCourseScopes) ServiceUtils.executeService(
                    userView, "ReadActiveCurricularCourseScopeByDegreeCurricularPlanAndExecutionYear",
                    args2);
        } catch (FenixServiceException fse) {
            throw new FenixActionException(fse);
        }
        if (result.getScopes() == null || result.getScopes().size() == 0) {
            errors.add("noDegreeCurricularPlan", new ActionError(
                    "error.nonExisting.AssociatedCurricularCourses"));
            saveErrors(request, errors);
        }

        request.setAttribute("degreeCurricularPlan", result.getInfoDegreeCurricularPlan());

        if (errors.isEmpty()) {
            List activeCurricularCourseScopes = groupScopesByCurricularYearAndCurricularCourse(result
                    .getScopes());

            request.setAttribute("allActiveCurricularCourseScopes", activeCurricularCourseScopes);
            request.setAttribute("executionYearID", executionYearID);
        }
        return mapping.findForward("showActiveCurricularCourses");
    }

    private List groupScopesByCurricularYearAndCurricularCourse(List scopes) {
        List result = new ArrayList();
        List temp = new ArrayList();

        ComparatorChain comparatorChain = new ComparatorChain();
        comparatorChain.addComparator(new BeanComparator(
                "infoCurricularSemester.infoCurricularYear.year"));
        comparatorChain.addComparator(new BeanComparator("infoCurricularSemester.semester"));
        comparatorChain.addComparator(new BeanComparator("infoCurricularCourse.name", Collator
                .getInstance()));
        Collections.sort(scopes, comparatorChain);

        if (scopes != null && scopes.size() > 0) {
            ListIterator iter = scopes.listIterator();
            InfoCurricularYear year = null;
            InfoCurricularCourse curricularCourse = null;

            while (iter.hasNext()) {
                InfoCurricularCourseScope scope = (InfoCurricularCourseScope) iter.next();
                InfoCurricularYear scopeYear = scope.getInfoCurricularSemester().getInfoCurricularYear();
                InfoCurricularCourse scopeCurricularCourse = scope.getInfoCurricularCourse();
                if (year == null) {
                    year = scopeYear;
                }
                if (curricularCourse == null) {
                    curricularCourse = scopeCurricularCourse;
                }

                if (scopeYear.equals(year) && scopeCurricularCourse.equals(curricularCourse)) {
                    temp.add(scope);
                } else {
                    result.add(temp);
                    temp = new ArrayList();
                    year = scopeYear;
                    curricularCourse = scopeCurricularCourse;
                    temp.add(scope);
                }

                if (!iter.hasNext()) {
                    result.add(temp);
                }
            }
        }

        return result;
    }
}
/*
 * Created on 3/Dez/2003
 *  
 */
package ServidorApresentacao.Action.manager;

import java.text.Collator;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.DispatchAction;

import DataBeans.InfoDegree;
import DataBeans.InfoExecutionPeriod;
import ServidorAplicacao.IUserView;
import ServidorAplicacao.Servico.exceptions.FenixServiceException;
import ServidorApresentacao.Action.exceptions.FenixActionException;
import ServidorApresentacao.Action.sop.utils.ServiceUtils;
import ServidorApresentacao.Action.sop.utils.SessionConstants;
import ServidorApresentacao.Action.sop.utils.SessionUtils;

/**
 * @author <a href="mailto:joao.mota@ist.utl.pt">Jo�o Mota </a> 3/Dez/2003
 * @author Fernanda Quit�rio 17/Dez/2003
 * 
 */
public class MergeExecutionCourseDispatchionAction extends DispatchAction {

    public ActionForward chooseDegreesAndExecutionPeriod(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws FenixActionException,
            FenixServiceException {

        DynaActionForm degreesForm = (DynaActionForm) form;
        Integer sourceDegreeId = (Integer) degreesForm.get("sourceDegreeId");
        Integer destinationDegreeId = (Integer) degreesForm.get("destinationDegreeId");
        Integer executionPeriodId = (Integer) degreesForm.get("executionPeriodId");

        getSourceAndDestinationExecutionCourses(request, sourceDegreeId, destinationDegreeId,
                executionPeriodId);

        getSourceAndDestinationDegrees(request, sourceDegreeId, destinationDegreeId);

        getExecutionPeriod(request, executionPeriodId);

        return mapping.findForward("chooseExecutionCourses");
    }

    protected void getExecutionPeriod(HttpServletRequest request, Integer executionPeriodId)
            throws FenixServiceException {
        IUserView userView = SessionUtils.getUserView(request);

        Object[] args = { executionPeriodId };

        InfoExecutionPeriod infoExecutionPeriod = (InfoExecutionPeriod) ServiceUtils.executeService(
                userView, "ReadExecutionPeriodByOID", args);

        request.setAttribute("infoExecutionPeriod", infoExecutionPeriod);
    }

    protected void getSourceAndDestinationDegrees(HttpServletRequest request, Integer sourceDegreeId,
            Integer destinationDegreeId) throws FenixServiceException {
        IUserView userView = SessionUtils.getUserView(request);

        Object[] args1 = { sourceDegreeId };
        Object[] args2 = { destinationDegreeId };

        InfoDegree sourceInfoDegree = (InfoDegree) ServiceUtils.executeService(userView,
                "ReadDegreeByOID", args1);
        InfoDegree destinationInfoDegree = (InfoDegree) ServiceUtils.executeService(userView,
                "ReadDegreeByOID", args2);

        request.setAttribute("sourceInfoDegree", sourceInfoDegree);
        request.setAttribute("destinationInfoDegree", destinationInfoDegree);
    }

    protected void getSourceAndDestinationExecutionCourses(HttpServletRequest request,
            Integer sourceDegreeId, Integer destinationDegreeId, Integer executionPeriodId)
            throws FenixServiceException {
        IUserView userView = SessionUtils.getUserView(request);

        Object[] args1 = { destinationDegreeId, executionPeriodId };
        Object[] args2 = { sourceDegreeId, executionPeriodId };
        List destinationExecutionCourses = (List) ServiceUtils.executeService(userView,
                "ReadExecutionCoursesByDegreeAndExecutionPeriodId", args1);
        List sourceExecutionCourses = (List) ServiceUtils.executeService(userView,
                "ReadExecutionCoursesByDegreeAndExecutionPeriodId", args2);

        Collator collator = Collator.getInstance();
        Collections.sort(destinationExecutionCourses, new BeanComparator("nome", collator));
        Collections.sort(sourceExecutionCourses, new BeanComparator("nome", collator));

        request.setAttribute("sourceExecutionCourses", sourceExecutionCourses);
        request.setAttribute("destinationExecutionCourses", destinationExecutionCourses);
    }

    public ActionForward prepareChooseDegreesAndExecutionPeriod(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws FenixActionException,
            FenixServiceException {
        HttpSession session = request.getSession(false);
        IUserView userView = (IUserView) session.getAttribute(SessionConstants.U_VIEW);

        Object[] args = {};

        List degrees = (List) ServiceUtils.executeService(userView, "ReadDegrees", args);
        List executionPeriods = (List) ServiceUtils.executeService(userView, "ReadAllExecutionPeriods",
                args);

        ComparatorChain comparator = new ComparatorChain();
        comparator.addComparator(new BeanComparator("infoExecutionYear.year"), true);
        comparator.addComparator(new BeanComparator("name"), true);
        Collections.sort(executionPeriods, comparator);

        Collections.sort(degrees, new BeanComparator("sigla"));

        request.setAttribute("sourceDegrees", degrees);
        request.setAttribute("destinationDegrees", degrees);
        request.setAttribute("executionPeriods", executionPeriods);

        return mapping.findForward("chooseDegreesAndExecutionPeriod");
    }

    public ActionForward mergeExecutionCourses(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws FenixActionException,
            FenixServiceException {
        HttpSession session = request.getSession(false);
        IUserView userView = (IUserView) session.getAttribute(SessionConstants.U_VIEW);
        DynaActionForm mergeExecutionCoursesForm = (DynaActionForm) form;
        Integer sourceExecutionCourseId = (Integer) mergeExecutionCoursesForm
                .get("sourceExecutionCourseId");
        Integer destinationExecutionCourseId = (Integer) mergeExecutionCoursesForm
                .get("destinationExecutionCourseId");
        Object[] args = { destinationExecutionCourseId, sourceExecutionCourseId };

        ServiceUtils.executeService(userView, "MergeExecutionCourses", args);

        return mapping.findForward("sucess");
    }
}
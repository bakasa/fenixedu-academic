/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.domain.phd.candidacy;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.period.CandidacyPeriod;
import org.fenixedu.academic.domain.phd.PhdIndividualProgramProcess;
import org.fenixedu.academic.domain.phd.PhdProgram;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.academic.util.LocaleUtils;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.academic.util.phd.InstitutionPhdCandidacyProcessProperties;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.template.DeclareMessageTemplate;
import org.fenixedu.messaging.core.template.TemplateParameter;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

@DeclareMessageTemplate(id = "phd.referee.link.email.message.template",
        description = "phd.referee.link.email.message.description",
        subject = "phd.referee.link.email.message.subject",
        text = "phd.referee.link.email.message.body",
        parameters = {
                @TemplateParameter(id = "candidateName", description = "phd.referee.link.email.message.parameter.candidateName"),
                @TemplateParameter(id = "institutionName", description = "phd.referee.link.email.message.parameter.institutionName"),
                @TemplateParameter(id = "refereeLink", description = "phd.referee.link.email.message.parameter.refereeLink"),
                @TemplateParameter(id = "hashCodeValue", description = "phd.referee.link.email.message.parameter.hashCodeValue"),
                @TemplateParameter(id = "programName", description = "phd.referee.link.email.message.parameter.programName")
        },
        bundle = Bundle.PHD
)
public class InstitutionPhdCandidacyPeriod extends InstitutionPhdCandidacyPeriod_Base {

    protected InstitutionPhdCandidacyPeriod() {
        super();
    }

    protected InstitutionPhdCandidacyPeriod(final ExecutionYear executionYear, final DateTime start, final DateTime end,
            final PhdCandidacyPeriodType type) {
        this();
        init(executionYear, start, end, type);
    }

    @Override
    protected void init(ExecutionYear executionYear, DateTime start, DateTime end, PhdCandidacyPeriodType type) {
        checkOverlapingDates(start, end, type);

        if (!PhdCandidacyPeriodType.INSTITUTION.equals(type)) {
            throw new DomainException("error.InstitutionPhdCandidacyPeriod.type.must.be.institution");
        }

        super.init(executionYear, start, end, type);
    }

    @Override
    public boolean isInstitutionCandidacyPeriod() {
        return true;
    }

    @Atomic
    public void addPhdProgramToPeriod(final PhdProgram phdProgram) {
        if (phdProgram == null) {
            throw new DomainException("phd.InstitutionPhdCandidacyPeriod.phdProgram.required");
        }

        super.addPhdPrograms(phdProgram);
    }

    @Atomic
    public void addPhdProgramListToPeriod(final List<PhdProgram> phdProgramList) {
        super.getPhdProgramsSet().addAll(phdProgramList);
    }

    @Atomic
    public void removePhdProgramInPeriod(final PhdProgram phdProgram) {
        if (phdProgram == null) {
            throw new DomainException("phd.InstitutionPhdCandidacyPeriod.phdProgram.required");
        }

        super.removePhdPrograms(phdProgram);
    }

    @Override
    public void addPhdPrograms(PhdProgram phdPrograms) {
        throw new DomainException("call addPhdProgramToPeriod()");
    }

    @Override
    public void removePhdPrograms(PhdProgram phdPrograms) {
        throw new DomainException("call removePhdProgramInPeriod()");
    }

    @Atomic
    public static InstitutionPhdCandidacyPeriod create(final PhdCandidacyPeriodBean phdCandidacyPeriodBean) {
        final ExecutionYear executionYear = phdCandidacyPeriodBean.getExecutionYear();
        final DateTime start = phdCandidacyPeriodBean.getStart();
        final DateTime end = phdCandidacyPeriodBean.getEnd();
        final PhdCandidacyPeriodType type = phdCandidacyPeriodBean.getType();

        return new InstitutionPhdCandidacyPeriod(executionYear, start, end, type);
    }

    public static InstitutionPhdCandidacyPeriod readInstitutionPhdCandidacyPeriodForDate(final DateTime date) {
        for (final CandidacyPeriod period : Bennu.getInstance().getCandidacyPeriodsSet()) {
            if (period.isInstitutionCandidacyPeriod() && period.contains(date)) {
                return (InstitutionPhdCandidacyPeriod) period;
            }
        }

        return null;
    }

    public static boolean isAnyInstitutionPhdCandidacyPeriodActive(final DateTime date) {
        return readInstitutionPhdCandidacyPeriodForDate(date) != null;
    }

    public static boolean isAnyInstitutionPhdCandidacyPeriodActive() {
        return isAnyInstitutionPhdCandidacyPeriodActive(new DateTime());
    }

    static public InstitutionPhdCandidacyPeriod getMostRecentCandidacyPeriod() {
        PhdCandidacyPeriod mostRecentCandidacyPeriod = null;

        for (CandidacyPeriod candidacyPeriod : Bennu.getInstance().getCandidacyPeriodsSet()) {
            if (!candidacyPeriod.isInstitutionCandidacyPeriod()) {
                continue;
            }

            if (candidacyPeriod.getStart().isAfterNow()) {
                continue;
            }

            if (mostRecentCandidacyPeriod == null) {
                mostRecentCandidacyPeriod = (PhdCandidacyPeriod) candidacyPeriod;
                continue;
            }

            if (candidacyPeriod.getStart().isAfter(mostRecentCandidacyPeriod.getStart())) {
                mostRecentCandidacyPeriod = (PhdCandidacyPeriod) candidacyPeriod;
            }
        }

        return (InstitutionPhdCandidacyPeriod) mostRecentCandidacyPeriod;
    }

    // TODO: remove this when PHD alerts are fully migrated to messaging MessageTemplates
    @Override
    public String getEmailMessageBodyForRefereeForm(final PhdCandidacyReferee referee) {
        final ExecutionYear executionYear = ExecutionYear.readByDateTime(referee.getPhdProgramCandidacyProcess().getCandidacyDate());
        return MessageFormat.format(String.format(BundleUtil.getString(Bundle.PHD, "message.phd.institution.email.body.referee"),
                referee.getPhdProgramCandidacyProcess().getPhdProgram().getName(executionYear).getContent(LocaleUtils.EN),
                InstitutionPhdCandidacyProcessProperties.getPublicCandidacyRefereeFormLink(new Locale("en", "EN")),
                referee.getValue(),
                referee.getPhdProgramCandidacyProcess().getPhdProgram().getName(executionYear).getContent(LocaleUtils.PT),
                InstitutionPhdCandidacyProcessProperties.getPublicCandidacyRefereeFormLink(new Locale("pt", "PT")),
                referee.getValue()), Unit.getInstitutionName().getContent());
    }

    @Override
    public void sendEmailForRefereeForm(final PhdCandidacyReferee referee) {

        final ExecutionYear executionYear = ExecutionYear.readByDateTime(referee.getPhdProgramCandidacyProcess().getCandidacyDate());
        final String programName = referee.getPhdProgramCandidacyProcess().getPhdProgram().getName(executionYear).getContent(I18N.getLocale());
        final String refereeLink = InstitutionPhdCandidacyProcessProperties.getPublicCandidacyRefereeFormLink(I18N.getLocale());

        Message.fromSystem().replyToSender()
                .singleBcc(referee.getEmail())
                .template("phd.referee.link.email.message.template")
                .parameter("institutionName", Unit.getInstitutionName().getContent(I18N.getLocale()))
                .parameter("refereeLink", refereeLink)
                .parameter("hashCodeValue", referee.getValue())
                .parameter("programName", programName)
                .parameter("candidateName", referee.getCandidatePerson().getName())
                .and()
                .wrapped().send();
    }

    @Override
    public LocalizedString getEmailMessageSubjectForMissingCandidacyValidation(PhdIndividualProgramProcess process) {
        return new LocalizedString().with(
                LocaleUtils.PT,
                MessageFormat.format(BundleUtil.getString(Bundle.PHD, Locale.forLanguageTag("pt"),
                        "message.phd.institution.email.subject.missing.candidacy.validation"), Unit.getInstitutionAcronym()))
                .with(LocaleUtils.EN,
                        MessageFormat.format(BundleUtil.getString(Bundle.PHD, Locale.ENGLISH,
                                "message.phd.institution.email.subject.missing.candidacy.validation"), Unit
                                .getInstitutionAcronym()));
    }

    @Override
    public LocalizedString getEmailMessageBodyForMissingCandidacyValidation(PhdIndividualProgramProcess process) {
        final String englishBody =
                MessageFormat.format(String.format(BundleUtil.getString(Bundle.PHD, Locale.ENGLISH,
                        "message.phd.institution.email.body.missing.candidacy.validation"),
                        InstitutionPhdCandidacyProcessProperties.getPublicCandidacyAccessLink(new Locale("en", "EN")), process
                                .getCandidacyProcess().getCandidacyHashCode().getValue()), Unit.getInstitutionAcronym());
        final String portugueseBody =
                MessageFormat.format(String.format(BundleUtil.getString(Bundle.PHD, Locale.forLanguageTag("pt"),
                        "message.phd.institution.email.body.missing.candidacy.validation"),
                        InstitutionPhdCandidacyProcessProperties.getPublicCandidacyAccessLink(new Locale("en", "EN")), process
                                .getCandidacyProcess().getCandidacyHashCode().getValue()), Unit.getInstitutionAcronym());

        return new LocalizedString().with(LocaleUtils.EN, englishBody).with(LocaleUtils.PT, portugueseBody);
    }

    public static InstitutionPhdCandidacyPeriod readNextCandidacyPeriod() {
        List<PhdCandidacyPeriod> readOrderedPhdCandidacyPeriods = readOrderedPhdCandidacyPeriods();

        for (PhdCandidacyPeriod phdCandidacyPeriod : readOrderedPhdCandidacyPeriods) {
            if (!phdCandidacyPeriod.isInstitutionCandidacyPeriod()) {
                continue;
            }

            if (phdCandidacyPeriod.getStart().isAfterNow()) {
                return (InstitutionPhdCandidacyPeriod) phdCandidacyPeriod;
            }
        }

        return null;
    }

}

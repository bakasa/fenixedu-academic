package net.sourceforge.fenixedu.domain.organizationalStructure;

import java.util.Date;

import net.sourceforge.fenixedu.domain.RootDomainObject;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.util.DateFormatUtil;

public class PersonFunction extends PersonFunction_Base {

    public PersonFunction() {
		super();
		setRootDomainObject(RootDomainObject.getInstance());
	}

	public void edit(Function function, Date beginDate, Date endDate, Double credits) {
        this.setFunction(function);
        this.setCredits(credits);        
        this.setBeginDate(beginDate);
        if(endDate != null && endDate.before(beginDate)){            
            throw new DomainException("error.endDateBeforeBeginDate");
        }
        this.setEndDate(endDate);
    }
    
    public void delete(){
        removeFunction();
        removePerson();
        deleteDomainObject();
    }

    public boolean isActive(Date currentDate) {
        if (this.getEndDate() == null
                || (DateFormatUtil.equalDates("yyyyMMdd", this.getEndDate(), currentDate) || this
                        .getEndDate().after(currentDate))) {
            return true;
        }
        return false;
    }
    
    public boolean belongsToPeriod(Date beginDate, Date endDate) {
        if (!this.getBeginDate().after(endDate)
                && (this.getEndDate() == null || !this.getEndDate().before(beginDate))) {
            return true;
        }
        return false;
    }
}

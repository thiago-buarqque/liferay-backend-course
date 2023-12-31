package com.liferay.training.gradebook.validator.util;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.training.gradebook.configuration.GradebookSystemServiceConfiguration;
import com.liferay.training.gradebook.exception.AssignmentValidationException;
import com.liferay.training.gradebook.validator.AssignmentValidator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
        configurationPid = "com.liferay.training.gradebook.configuration.GradebookSystemServiceConfiguration",
        immediate = true,
        service = AssignmentValidator.class
)
public class AssignmentValidatorImpl implements AssignmentValidator {


    @Activate
    @Modified
    private void activate(Map<String, Object> properties) {
        _moduleConfiguration = ConfigurableUtil.createConfigurable(
                GradebookSystemServiceConfiguration.class, properties);
    }

    /**
     * Validates assignment values and throws
     * {AssignmentValidationExceptionException} if the assignment values are not
     * valid.
     *
     * @param titleMap
     * @param descriptionMap
     * @param dueDate
     * @throws AssignmentValidationException
     */
    public void validate(
            Map<Locale, String> titleMap, Map<Locale, String> descriptionMap, Date dueDate)
            throws AssignmentValidationException {

        List<String> errors = new ArrayList<>();

        if (!isAssignmentValid(titleMap, descriptionMap, dueDate, errors)) {
            throw new AssignmentValidationException(errors);
        }
    }

    /**
     * Returns <code>true</code> if the given fields are valid for an
     * assignment.
     *
     * @param titleMap
     * @param descriptionMap
     * @param dueDate
     * @param errors
     * @return boolean <code>true</code> if assignment is valid, otherwise
     *         <code>false</code>
     */

    private boolean isAssignmentValid(
            final Map<Locale, String> titleMap, final Map<Locale, String> descriptionMap,
            final Date dueDate, final List<String> errors) {

        boolean result = true;

        result &= isTitleValid(titleMap, errors);
        result &= isDueDateValid(dueDate, errors);
        result &= isDescriptionValid(descriptionMap, errors);

        return result;
    }

    /**
     * Returns <code>true</code> if description is valid for an assignment. If
     * not valid, an error message is added to the errors list.
     *
     * @param descriptionMap
     * @param errors
     * @return boolean <code>true</code> if description is valid, otherwise
     *         <code>false</code>
     */
    private boolean isDescriptionValid(
            final Map<Locale, String> descriptionMap, final List<String> errors) {

        // Verify the map has something
        if (MapUtil.isEmpty(descriptionMap)) {
            errors.add("assignmentDescriptionEmpty");
            return false;
        }
        else {
            // Get the default locale
            Locale defaultLocale = LocaleUtil.getSiteDefault();
            String description = descriptionMap.get(defaultLocale);

            if ((Validator.isBlank(description))) {
                errors.add("assignmentDescriptionEmpty");
                return false;
            }
            // Strip HTML tags from text.
            String descriptionText = HtmlUtil.stripHtml(description);
            if (Validator.isBlank(descriptionText)) {
                errors.add("assignmentDescriptionEmpty");
                return false;
            }
            if (descriptionText.length() < _moduleConfiguration.descriptionMinLength()) {
                errors.add("assignmentDescriptionTooShort");
                return false;
            }
            else if (descriptionText.length() > _moduleConfiguration.descriptionMaxLength()) {
                errors.add("assignmentDescriptionTooLong");
                return false;
            }
        }

        return true;
    }

    /**
     * Returns <code>true</code> if due date is valid for an assignment. If not
     * valid, an error message is added to the errors list.
     * Note that this error can't ever happen in the user interface because
     * we are always getting a default value on the controller layer (Action Commands)
     * However, this service could be access through the WS Api, which is why we need it.
     *
     * @param dueDate
     * @param errors
     * @return boolean <code>true</code> if due date is valid, otherwise
     *         <code>false</code>
     */
    private boolean isDueDateValid(
            final Date dueDate, final List<String> errors) {

        boolean result = true;

        if (Validator.isNull(dueDate)) {
            errors.add("assignmentDateEmpty");
            result = false;
        }

        return result;
    }

    /**
     * Returns <code>true</code> if title is valid for an assignment. If not
     * valid, an error message is added to the errors list.
     *
     * @param titleMap
     * @param errors
     * @return boolean <code>true</code> if the title is valid, otherwise
     *         <code>false</code>
     */

    private boolean isTitleValid(
            final Map<Locale, String> titleMap, final List<String> errors) {

        // Verify the map has something
        if (MapUtil.isEmpty(titleMap)) {
            errors.add("assignmentTitleEmpty");
            return false;
        }
        else {
            // Get the default locale
            Locale defaultLocale = LocaleUtil.getSiteDefault();
            String title = titleMap.get(defaultLocale);

            if ((Validator.isBlank(title))) {
                errors.add("assignmentTitleEmpty");
                return false;
            }
            // Strip HTML tags from text.
            String descriptionText = HtmlUtil.stripHtml(title);
            if (Validator.isBlank(descriptionText)) {
                errors.add("assignmentTitleEmpty");
                return false;
            }
            if (descriptionText.length() < _moduleConfiguration.titleMinLength()) {
                errors.add("assignmentTitleTooShort");
                return false;
            }
            else if (descriptionText.length() > _moduleConfiguration.titleMaxLength()) {
                errors.add("assignmentTitleTooLong");
                return false;
            }
        }

        return true;
    }

    private volatile GradebookSystemServiceConfiguration _moduleConfiguration;
}

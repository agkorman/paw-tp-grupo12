package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.webapp.validation.ValidCommunityForm;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@ValidCommunityForm
public class CommunityForm {

    @NotBlank(message = "{validation.community.name.required}")
    @Size(max = 21, message = "{validation.community.name.max}")
    private String name;

    @NotBlank(message = "{validation.community.description.required}")
    @Size(max = 180, message = "{validation.community.description.max}")
    private String description;

    @NotEmpty(message = "{validation.community.topics.required}")
    @Size(max = 4, message = "{validation.community.topics.max}")
    private Set<Short> selectedTopicIds = new LinkedHashSet<>();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Set<Short> getSelectedTopicIds() {
        return selectedTopicIds;
    }

    public void setSelectedTopicIds(final Set<Short> selectedTopicIds) {
        this.selectedTopicIds = selectedTopicIds == null ? new LinkedHashSet<>() : selectedTopicIds;
    }
}

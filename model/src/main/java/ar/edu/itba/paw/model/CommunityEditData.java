package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommunityEditData implements Serializable {

    private final Community community;
    private final List<CommunityTopic> selectedTopics;
    private final boolean viewerCreator;

    public CommunityEditData(final Community community, final List<CommunityTopic> selectedTopics,
                             final boolean viewerCreator) {
        this.community = community;
        this.selectedTopics = selectedTopics == null ? new ArrayList<>() : new ArrayList<>(selectedTopics);
        this.viewerCreator = viewerCreator;
    }

    public boolean isViewerCreator() {
        return viewerCreator;
    }

    public Community getCommunity() {
        return community;
    }

    public List<CommunityTopic> getSelectedTopics() {
        return new ArrayList<>(selectedTopics);
    }
}

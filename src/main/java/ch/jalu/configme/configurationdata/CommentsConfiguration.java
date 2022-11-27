package ch.jalu.configme.configurationdata;

import ch.jalu.configme.SettingsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * Allows to register comments (intended via {@link SettingsHolder#registerComments}).
 */
public class CommentsConfiguration {

    private final @NotNull Map<String, List<String>> comments;
    private @NotNull List<String> footerComments;

    /**
     * Constructor.
     */
    public CommentsConfiguration() {
        this.comments = new HashMap<>();
        this.footerComments = new ArrayList<>();
    }

    /**
     * Constructor.
     *
     * @param comments map to store comments in
     */
    public CommentsConfiguration(@NotNull Map<String, List<String>> comments, @NotNull  List<String> footerComments) {
        this.comments = comments;
        this.footerComments = footerComments;
    }

    /**
     * Sets the given lines for the provided path, overriding any previously existing comments for the path.
     * An entry that is a sole new-line (i.e. "\n") will result in an empty line without any comment marker.
     *
     * @param path the path to register the comment lines for
     * @param commentLines the comment lines to set for the path
     */
    public void setComment(@NotNull String path, @NotNull String... commentLines) {
        comments.put(path, Collections.unmodifiableList(Arrays.asList(commentLines)));
    }

    /**
     * Sets the given lines for the provided path, overriding any previously existing comments for the path.
     * An entry that is a sole new-line (i.e. "\n") will result in an empty line without any comment marker.
     *
     * @param commentLines the comment lines to set for the path
     */
    public void setFooter(@NotNull String... commentLines) {
        footerComments = Collections.unmodifiableList(Arrays.asList(commentLines));
    }

    /**
     * Returns a read-only view of the map with all comments.
     *
     * @return map with all comments
     */
    public @NotNull @UnmodifiableView Map<String, @UnmodifiableView List<String>> getAllComments() {
        return Collections.unmodifiableMap(comments);
    }

    /**
     * Returns a read-only view of the list with all footer comments.
     *
     * @return list with all footer comments
     */
    public @NotNull @UnmodifiableView List<String> getFooterComments() {
        return Collections.unmodifiableList(footerComments);
    }
}

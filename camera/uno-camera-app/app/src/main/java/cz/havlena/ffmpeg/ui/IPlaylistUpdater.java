/**
 *
 */
package cz.havlena.ffmpeg.ui;

import java.util.ArrayList;

/**
 * @author Hoang
 */
public interface IPlaylistUpdater {

  void updatePlaylist (ArrayList<String> list);

  void finishLoadingPlaylist (boolean isFinishLoading);

}

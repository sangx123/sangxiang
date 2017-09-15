package com.blinkhd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.devcomm.Device;
import com.hubble.file.FileService;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubble.registration.ui.CommonDialogListener;

import com.koushikdutta.async.future.FutureCallback;
import com.nxcomm.blinkhd.ui.dialog.FileEventDialog;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import base.hubble.PublicDefineGlob;

import com.hubbleconnected.camera.R;

/**
 * Created by Sean on 2014-10-07.
 */
public class DeviceStorageFragment extends Fragment {
  private static final String TAG = "Camera Storage Fragment";

  private ListView mListview;
  private View mLoadingHolder;
  private TextView mLoadingText;
  private ProgressBar mLoadingBar;

  private Device device;
  private Activity mActivity;
  private List<String> mFilenames = new ArrayList<String>();
  private List<String> mDisplayNames = new ArrayList<String>();
  private ArrayAdapter<String> mAdapter;
  private DateTimeFormatter displayFormat = DateTimeFormat.forPattern("EEE MMM dd yyyy - hh:mm:ss aa");
  private DateTimeFormatter cameraFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");

  private File mFile;
  private Fragment mFragment;

  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View contentView = inflater.inflate(R.layout.camera_storage_fragment, container, false);
    mListview = (ListView) contentView.findViewById(R.id.camera_storage_listview);
    mLoadingHolder = contentView.findViewById(R.id.camera_storage_loading_holder);
    mLoadingText = (TextView) contentView.findViewById(R.id.camera_storage_loading_text);
    mLoadingBar = (ProgressBar) contentView.findViewById(R.id.camera_storage_loading_bar);

    if (device != null) {
      TextView filesTv = (TextView) contentView.findViewById(R.id.camera_storage_camera_name);
      filesTv.setText(mActivity.getString(R.string.files_on) + device.getProfile().getName());
      filesTv.setSelected(true);
    }

    SwipeDismissListViewTouchListener touchListener =
        new SwipeDismissListViewTouchListener(
            mListview,
            new SwipeDismissListViewTouchListener.DismissCallbacks() {
              @Override
              public boolean canDismiss(int position) {
                return true;
              }

              @Override
              public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                  String file = mFilenames.get(position);
                  String displayName = mAdapter.getItem(position);
                  mAdapter.remove(displayName);
                  new DeleteFileOnCamera().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
                }
                mAdapter.notifyDataSetChanged();
              }
            });
    mListview.setOnTouchListener(touchListener);
    // Setting this scroll listener is required to ensure that during ListView scrolling,
    // we don't look for swipes.
    mListview.setOnScrollListener(touchListener.makeScrollListener());
    return contentView;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mActivity = activity;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mFragment = this;

    new GetFilesOnCamera().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  public void setDevice(Device cameraProfile) {
    device = cameraProfile;
  }

  private class GetFilesOnCamera extends AsyncTask<Void, Void, List<String>> {

    @Override
    protected List<String> doInBackground(Void... params) {
      if (device != null) {
        final String device_ip = device.getProfile().getDeviceLocation().getLocalIp();
        String http_address = String.format("%1$s%2$s%3$s%4$s", "http://", device_ip, "/?action=command&command=", PublicDefineGlob.GET_SD_RECORDING_LIST);
        List<String> filenames = new ArrayList<String>();
        try {
          DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
          DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
          Document fileListDocument = dBuilder.parse(http_address);
          fileListDocument.getDocumentElement().normalize();
          NodeList nList = fileListDocument.getLastChild().getChildNodes();

          for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
              Element eElement = (Element) nNode;

              if (eElement.getTagName().contains("file")) {
                String tagName = eElement.getTagName();
                Node item = eElement.getFirstChild();
                String itemFileName = item.getTextContent();
                filenames.add(itemFileName);
              }
            }
          }

          return filenames;
        } catch (Exception e) {

        }
      }
      return null;
    }

    @Override
    protected void onPostExecute(final List<String> recordingListResult) {
      super.onPostExecute(recordingListResult);
      if (recordingListResult != null && recordingListResult.size() > 0) {
        mLoadingHolder.setVisibility(View.GONE);
        mFilenames = recordingListResult;
        try {
          for (String filename : mFilenames) {
            String displayName = filename.substring(13);
            displayName = displayName.substring(0, displayName.length() - 7);
            DateTime dateTime = cameraFormat.parseDateTime(displayName);
            displayName = displayFormat.print(dateTime);
            mDisplayNames.add(displayName);
          }
        } catch (Exception e) {
          // // Log.d(TAG, e.getLocalizedMessage());
          mDisplayNames = mFilenames;
        }
        mAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, mDisplayNames);
        mListview.setAdapter(mAdapter);
        mListview.setOnItemClickListener(mListItemClickListener);
      } else {
        mListview.setVisibility(View.GONE);
        mLoadingBar.setVisibility(View.GONE);
        mLoadingText.setText(mActivity.getString(R.string.could_not_retieve_files_from_camera_storage));
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    if (mFile != null) {
      mFile.delete();
    }
  }

  private class DeleteFileOnCamera extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... files) {
      String file = files[0];
      if (device != null) {
        final String device_ip = device.getProfile().getDeviceLocation().getLocalIp();
        String http_address = String.format("%1$s%2$s%3$s%4$s%5$s%6$s", "http://", device_ip, "/?action=command&command=", PublicDefineGlob.DELETE_SD_RECORDING, "&value=", file);
        String sdCardResponse = HTTPRequestSendRecvTask.sendRequest_block_for_response(http_address);

        return sdCardResponse;
      }
      return null;
    }

    @Override
    protected void onPostExecute(String didDelete) {
      super.onPostExecute(didDelete);
      if (mActivity != null) {
        if (didDelete == null) {
          Toast.makeText(getActivity(), mActivity.getString(R.string.could_not_delete_file), Toast.LENGTH_SHORT).show();
          new GetFilesOnCamera().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // refresh listview
          return;
        }
        didDelete = didDelete.substring(PublicDefineGlob.DELETE_SD_RECORDING.length() + 2);
        if (didDelete.equals("0")) {
          Toast.makeText(getActivity(), mActivity.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
        } else if (didDelete.equals("-1")) {
          Toast.makeText(getActivity(), mActivity.getString(R.string.could_not_delete_file), Toast.LENGTH_SHORT).show();
          new GetFilesOnCamera().execute(); // refresh listview
        }
      }
    }
  }

  private AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      if (device != null) {

        final String device_ip = device.getProfile().getDeviceLocation().getLocalIp();
        String http_address = String.format("%1$s%2$s%3$s%4$s%5$s%6$s", "http://", device_ip, "/?action=command&command=", PublicDefineGlob.DOWNLOAD_SD_RECORDING, "&value=", mFilenames.get(position));
        final ProgressDialog progressDialog = new ProgressDialog(mActivity);
        progressDialog.setMessage(mActivity.getString(R.string.getting_file_from_camera));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        FileService.downloadVideo(http_address, mFilenames.get(position), new FutureCallback<File>() {
          @Override
          public void onCompleted(Exception e, final File resultFile) {
            if (progressDialog != null && progressDialog.isShowing()) {
              try {
                progressDialog.dismiss();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
            if (e != null) {
              Log.e(TAG, "Cannot download file from camera");
              e.printStackTrace();
              if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    Toast.makeText(getActivity(), getActivity().getString(R.string.camera_failed_to_send_file), Toast.LENGTH_SHORT).show();
                  }
                });
              }

            } else {
              Log.i(TAG, "Download file to " + resultFile.getAbsolutePath());
              Log.i(TAG, "File size: " + resultFile.length() + " bytes");

              final FileEventDialog fileEventDialog = new FileEventDialog();
              fileEventDialog.setCommonDialogListener(new CommonDialogListener() {
                @Override
                public void onDialogPositiveClick(DialogFragment arg0) {
                  Log.i(TAG, "Download file path: " + resultFile.getAbsolutePath());
                  if (fileEventDialog.getSelectImageSource() == FileEventDialog.EVENT_VIEW) {
                    Intent playbackIntent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(resultFile));
                    playbackIntent.setDataAndType(Uri.fromFile(resultFile), "video/*");
                    getActivity().startActivity(Intent.createChooser(playbackIntent, getString(R.string.open_video_with)));
                  } else if (fileEventDialog.getSelectImageSource() == FileEventDialog.EVENT_SHARE) {
                    String additional = getResources().getString(R.string.vtech_inform_view_file);
                    Intent intent = FileService.getShareIntent(resultFile, additional);
                    getActivity().startActivity(intent);
                  } else if (fileEventDialog.getSelectImageSource() == FileEventDialog.EVENT_SAVE) {
                    mFile = null;
                    Toast.makeText(getActivity(), getString(R.string.save_event_to_device), Toast.LENGTH_SHORT).show();
                  }
                  try {
                    fileEventDialog.dismiss();
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                  } catch (Exception ignored) {
                  }
                }

                @Override
                public void onDialogNegativeClick(DialogFragment dialog) {
                  getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }

                @Override
                public void onDialogNeutral(DialogFragment dialog) {
                  getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }

              });

              fileEventDialog.show(getActivity().getSupportFragmentManager(), "Dialog_File_Event");
            }
          }
        }, progressDialog);
      }
    }
  };
}

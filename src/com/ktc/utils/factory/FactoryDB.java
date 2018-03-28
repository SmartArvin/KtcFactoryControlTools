package com.ktc.utils.factory;

import com.ktc.utils.factory.IFactoryDesk.AUDIO_PEQ_PARAM;
import com.ktc.utils.factory.IFactoryDesk.EN_MS_COLOR_TEMP;
import com.ktc.utils.factory.IFactoryDesk.EN_MS_COLOR_TEMP_INPUT_SOURCE;
import com.ktc.utils.factory.IFactoryDesk.EN_MS_VIDEOITEM;
import com.ktc.utils.factory.IFactoryDesk.EN_VD_SIGNALTYPE;
import com.ktc.utils.factory.IFactoryDesk.MAPI_VIDEO_ARC_Type;
import com.ktc.utils.factory.IFactoryDesk.MAX_DTV_Resolution_Info;
import com.ktc.utils.factory.IFactoryDesk.MAX_HDMI_Resolution_Info;
import com.ktc.utils.factory.IFactoryDesk.MAX_YPbPr_Resolution_Info;
import com.ktc.utils.factory.IFactoryDesk.MS_FACTORY_EXTERN_SETTING;
import com.ktc.utils.factory.IFactoryDesk.MS_FACTORY_SSC_SET;
import com.ktc.utils.factory.IFactoryDesk.MS_Factory_NS_VIF_SET;
import com.ktc.utils.factory.IFactoryDesk.MS_NLA_POINT;
import com.ktc.utils.factory.IFactoryDesk.MS_NLA_SETTING;
import com.ktc.utils.factory.IFactoryDesk.ST_FACTORY_PEQ_SETTING;
import com.ktc.utils.factory.IFactoryDesk.ST_MAPI_VIDEO_WINDOW_INFO;
import com.ktc.utils.factory.IFactoryDesk.T_MS_COLOR_TEMPEX;
import com.ktc.utils.factory.IFactoryDesk.T_MS_COLOR_TEMPEX_DATA;
import com.ktc.utils.factory.IFactoryDesk.T_MS_COLOR_TEMP_DATA;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.factory.vo.FactoryNsVdSet;
import com.mstar.android.tvapi.factory.vo.PqlCalibrationData;



public class FactoryDB {
    private static FactoryDB instance;
    private Context mContext = null;
    private ContentResolver cr = null;
    public static FactoryDB getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new FactoryDB(context);
        }
        return instance;
    }

    private FactoryDB(Context context)
    {
        mContext = context;
    }

    public void openDB()
    {
    }

    public void closeDB()
    {
        //factoryDB.close();
        //userSettingDB.close();
    }

    private ContentResolver getContentResolver(){
        if(cr == null){
            cr = mContext.getContentResolver();
        }
        return cr;
    }


    /**
     * for ADCAdjust
     * @param i
     *
     * @param sourceId
     * @return
     */
    public PqlCalibrationData queryADCAdjust(int i)
    {
        Cursor cursor = getContentResolver().query(
                            Uri.parse("content://mstar.tv.factory/adcadjust/sourceid/" + i), null, null, null, null);
        PqlCalibrationData model = new PqlCalibrationData();
        if (cursor.moveToNext())
        {
            model.redGain = cursor.getInt(cursor.getColumnIndex("u16RedGain"));
            model.greenGain = cursor.getInt(cursor.getColumnIndex("u16GreenGain"));
            model.blueGain = cursor.getInt(cursor.getColumnIndex("u16BlueGain"));
            model.redOffset = cursor.getInt(cursor.getColumnIndex("u16RedOffset"));
            model.greenOffset = cursor.getInt(cursor.getColumnIndex("u16GreenOffset"));
            model.blueOffset = cursor.getInt(cursor.getColumnIndex("u16BlueOffset"));
        }
        cursor.close();
        return model;
    }

    public void updateADCAdjust(PqlCalibrationData model, int sourceId)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("u16RedGain", model.redGain);
        vals.put("u16GreenGain", model.greenGain);
        vals.put("u16BlueGain", model.blueGain);
        vals.put("u16RedOffset", model.redOffset);
        vals.put("u16GreenOffset", model.greenOffset);
        vals.put("u16BlueOffset", model.blueOffset);
        try
        {
            ret = getContentResolver().update(Uri.parse("content://mstar.tv.factory/adcadjust/sourceid/" + sourceId),
                    vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_ADCAdjust ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_ADCAdjust_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * for NonLinearAdjust
     *
     * @param sourceId
     * @return
     */
    // public Object queryNonLinearAdjust(int curveTypeIndex){
    // Cursor cursor = db.query("NonLinearAdjust", null, "CurveTypeIndex = " +
    // curveTypeIndex, null, null, null, null);
    // if (cursor.moveToFirst()) {
    // Object other = new Object();
    // value = cursor.getInt(cursor.getColumnIndex("u8OSD_V0"));
    // value = cursor.getInt(cursor.getColumnIndex("u8OSD_V25"));
    // value = cursor.getInt(cursor.getColumnIndex("u8OSD_V50"));
    // value = cursor.getInt(cursor.getColumnIndex("u8OSD_V75"));
    // value = cursor.getInt(cursor.getColumnIndex("u8OSD_V100"));
    // }
    // cursor.close();
    // return null;
    // }
    public MS_NLA_SETTING queryNonLinearAdjusts()
    {
        MS_NLA_SETTING model = new MS_NLA_SETTING();
        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.factory/nonlinearadjust"),
                null, "InputSrcType = " + queryCurInputSrc(), null, "CurveTypeIndex");
        int i = 0;
        int length = model.stNLASetting.length;
        while (cursor.moveToNext())
        {
            if (i > length - 1)
            {
                break;
            }
            model.stNLASetting[i].u8OSD_V0 = cursor.getInt(cursor.getColumnIndex("u8OSD_V0"));
            model.stNLASetting[i].u8OSD_V25 = cursor.getInt(cursor.getColumnIndex("u8OSD_V25"));
            model.stNLASetting[i].u8OSD_V50 = cursor.getInt(cursor.getColumnIndex("u8OSD_V50"));
            model.stNLASetting[i].u8OSD_V75 = cursor.getInt(cursor.getColumnIndex("u8OSD_V75"));
            model.stNLASetting[i].u8OSD_V100 = cursor.getInt(cursor.getColumnIndex("u8OSD_V100"));
            i++;
        }
        cursor.close();
        return model;
    }

    public void updateNonLinearAdjust(MS_NLA_POINT dataModel, int curveTypeIndex)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("u8OSD_V0", dataModel.u8OSD_V0);
        vals.put("u8OSD_V25", dataModel.u8OSD_V25);
        vals.put("u8OSD_V50", dataModel.u8OSD_V50);
        vals.put("u8OSD_V75", dataModel.u8OSD_V75);
        vals.put("u8OSD_V100", dataModel.u8OSD_V100);
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.factory/nonlinearadjust/inputsrctype/" + queryCurInputSrc()
                            + "/curvetypeindex/" + curveTypeIndex), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_NonLinearAdjust ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_NonLinearAdjust_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * for NonStandardAdjust
     *
     * @param sourceId
     * @return
     */
    public FactoryNsVdSet queryNoStandSet()
    {
        FactoryNsVdSet model = new FactoryNsVdSet();
        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.factory/nonstandardadjust"),
                null, null, null, null);
        if (cursor.moveToFirst())
        {
            model.aFEC_D4 = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_D4"));
            model.aFEC_D5_Bit2 = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_D5_Bit2"));
            model.aFEC_D8_Bit3210 = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_D8_Bit3210"));
            model.aFEC_D9_Bit0 = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_D9_Bit0"));
            model.aFEC_D7_LOW_BOUND = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_D7_LOW_BOUND"));
            model.aFEC_D7_HIGH_BOUND = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_D7_HIGH_BOUND"));
            model.aFEC_A0 = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_A0"));
            model.aFEC_A1 = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_A1"));
            model.aFEC_66_Bit76 = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_66_Bit76"));
            model.aFEC_6E_Bit7654 = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_6E_Bit7654"));
            model.aFEC_6E_Bit3210 = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_6E_Bit3210"));
            model.aFEC_43 = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_43"));
            model.aFEC_44 = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_44"));
            model.aFEC_CB = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_CB"));
            model.aFEC_CF_Bit2_ATV = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_CF_Bit2_ATV"));
            model.aFEC_CF_Bit2_AV = (short) cursor.getInt(cursor.getColumnIndex("u8AFEC_CF_Bit2_AV"));
        }
        cursor.close();
        return model;
    }

    public MS_Factory_NS_VIF_SET queryNoStandVifSet()
    {
        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.factory/nonstandardadjust"),
                null, null, null, null);
        MS_Factory_NS_VIF_SET model = new MS_Factory_NS_VIF_SET();
        if (cursor.moveToFirst())
        {
            model.VifTop = (short) cursor.getInt(cursor.getColumnIndex("VifTop"));
            model.VifVgaMaximum = cursor.getInt(cursor.getColumnIndex("VifVgaMaximum"));
            model.VifCrKp = (short) cursor.getInt(cursor.getColumnIndex("VifCrKp"));
            model.VifCrKi = (short) cursor.getInt(cursor.getColumnIndex("VifCrKi"));
            model.VifCrKp1 = (short) cursor.getInt(cursor.getColumnIndex("VifCrKp1"));
            model.VifCrKi1 = (short) cursor.getInt(cursor.getColumnIndex("VifCrKi1"));
            model.VifCrKp2 = (short) cursor.getInt(cursor.getColumnIndex("VifCrKp2"));
            model.VifCrKi2 = (short) cursor.getInt(cursor.getColumnIndex("VifCrKi2"));
            model.VifAsiaSignalOption = cursor.getInt(cursor.getColumnIndex("VifAsiaSignalOption")) == 0 ? false
                    : true;
            model.VifCrKpKiAdjust = (short) cursor.getInt(cursor.getColumnIndex("VifCrKpKiAdjust")) == 0 ? false
                    : true;
            model.VifOverModulation = cursor.getInt(cursor.getColumnIndex("VifOverModulation")) == 0 ? false : true;
            model.VifClampgainGainOvNegative = cursor.getInt(cursor.getColumnIndex("VifClampgainGainOvNegative"));
            model.ChinaDescramblerBox = (short) cursor.getInt(cursor.getColumnIndex("ChinaDescramblerBox"));
            model.ChinaDescramblerBoxDelay =cursor.getInt(cursor.getColumnIndex("ChinaDescramblerBoxDelay"));
            model.VifDelayReduce = (short) cursor.getInt(cursor.getColumnIndex("VifDelayReduce"));
            model.VifCrThr = (short) cursor.getInt(cursor.getColumnIndex("VifCrThr"));
            model.VifVersion = (short) cursor.getInt(cursor.getColumnIndex("VifVersion"));
            model.VifACIAGCREF = (short) cursor.getInt(cursor.getColumnIndex("VifACIAGCREF"));
            model.VifAgcRefNegative = (short) cursor.getInt(cursor.getColumnIndex("VifAgcRefNegative"));
            model.GainDistributionThr = cursor.getInt(cursor.getColumnIndex("GainDistributionThr"));
        }
        cursor.close();
        return model;
    }

    public void updateNonStandardAdjust(FactoryNsVdSet model)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("u8AFEC_D4", model.aFEC_D4);
        vals.put("u8AFEC_D5_Bit2", model.aFEC_D5_Bit2);
        vals.put("u8AFEC_D8_Bit3210", model.aFEC_6E_Bit3210);
        vals.put("u8AFEC_D9_Bit0", model.aFEC_D9_Bit0);
        vals.put("u8AFEC_D7_LOW_BOUND", model.aFEC_D7_LOW_BOUND);
        vals.put("u8AFEC_D7_HIGH_BOUND", model.aFEC_D7_HIGH_BOUND);
        vals.put("u8AFEC_A0", model.aFEC_A0);
        vals.put("u8AFEC_A1", model.aFEC_A1);
        vals.put("u8AFEC_66_Bit76", model.aFEC_66_Bit76);
        vals.put("u8AFEC_6E_Bit7654", model.aFEC_6E_Bit7654);
        vals.put("u8AFEC_6E_Bit3210", model.aFEC_6E_Bit3210);
        vals.put("u8AFEC_43", model.aFEC_43);
        vals.put("u8AFEC_44", model.aFEC_44);
        vals.put("u8AFEC_CB", model.aFEC_CB);
        vals.put("u8AFEC_CF_Bit2_ATV", model.aFEC_CF_Bit2_ATV);
        vals.put("u8AFEC_CF_Bit2_AV", model.aFEC_CF_Bit2_AV);
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.factory/nonstandardadjust"), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_NonLinearAdjust AFEC ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_NonStarndardAdjust_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void updateNonStandardAdjust(MS_Factory_NS_VIF_SET vifSet)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("VifTop", vifSet.VifTop);
        vals.put("VifVgaMaximum", vifSet.VifVgaMaximum);
        vals.put("VifCrKp", vifSet.VifCrKp);
        vals.put("VifCrKi", vifSet.VifCrKi);
        vals.put("VifCrKp1", vifSet.VifCrKp1);
        vals.put("VifCrKi1", vifSet.VifCrKi1);
        vals.put("VifCrKp2", vifSet.VifCrKp2);
        vals.put("VifCrKi2", vifSet.VifCrKi2);
        vals.put("VifAsiaSignalOption", vifSet.VifAsiaSignalOption ? 1 : 0);
        vals.put("VifCrKpKiAdjust", vifSet.VifCrKpKiAdjust ? 1 : 0);
        vals.put("VifOverModulation", vifSet.VifOverModulation ? 1 : 0);
        vals.put("VifClampgainGainOvNegative", vifSet.VifClampgainGainOvNegative);
        vals.put("ChinaDescramblerBox", vifSet.ChinaDescramblerBox);
        vals.put("ChinaDescramblerBoxDelay", vifSet.ChinaDescramblerBoxDelay);
        vals.put("VifDelayReduce", vifSet.VifDelayReduce);
        vals.put("VifCrThr", vifSet.VifCrThr);
        vals.put("VifVersion", vifSet.VifVersion);
        vals.put("VifACIAGCREF", vifSet.VifACIAGCREF);
        vals.put("VifAgcRefNegative", vifSet.VifAgcRefNegative);
        vals.put("GainDistributionThr", vifSet.GainDistributionThr);
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.factory/nonstandardadjust"), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_NonStandardAdjust Vif ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_NonStarndardAdjust_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * for factory extern
     *
     * @param sourceId
     * @return
     */
    public MS_FACTORY_EXTERN_SETTING queryFactoryExtern()
    {
        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.factory/factoryextern"),
                null, null, null, null);
        MS_FACTORY_EXTERN_SETTING model = new MS_FACTORY_EXTERN_SETTING();
        if (cursor.moveToFirst())
        {
            model.softVersion = cursor.getString(cursor.getColumnIndex("SoftWareVersion"));
            model.boardType = cursor.getString(cursor.getColumnIndex("BoardType"));
            model.panelType = cursor.getString(cursor.getColumnIndex("PanelType"));
            model.dayAndTime = cursor.getString(cursor.getColumnIndex("CompileTime"));
            model.testPatternMode = cursor.getInt(cursor.getColumnIndex("TestPatternMode"));
            model.stPowerMode = cursor.getInt(cursor.getColumnIndex("stPowerMode"));
            model.dtvAvAbnormalDelay = cursor.getInt(cursor.getColumnIndex("DtvAvAbnormalDelay")) == 0 ? false
                    : true;
            model.factoryPreset = cursor.getInt(cursor.getColumnIndex("FactoryPreSetFeature"));
            model.panelSwingVal = (short) cursor.getInt(cursor.getColumnIndex("PanelSwing"));
            model.audioPreScale = (short) cursor.getInt(cursor.getColumnIndex("AudioPrescale"));
//          model.st3DSelfAdaptiveLevel = (short) cursor.getInt(cursor.getColumnIndex("ThreeDSelfAdaptieveLevel"));
            model.vdDspVersion = (short) cursor.getInt(cursor.getColumnIndex("vdDspVersion"));
            model.eHidevMode = (short) cursor.getInt(cursor.getColumnIndex("eHidevMode"));
            model.audioNrThr = (short) cursor.getInt(cursor.getColumnIndex("audioNrThr"));
            model.audioSifThreshold = (short) cursor.getInt(cursor.getColumnIndex("audioSifThreshold"));
            model.audioDspVersion = (short) cursor.getInt(cursor.getColumnIndex("audioDspVersion"));
        }
        cursor.close();
        return model;
    }

    public void updateFactoryExtern(MS_FACTORY_EXTERN_SETTING model)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("SoftWareVersion", model.softVersion);
        vals.put("BoardType", model.boardType);
        vals.put("PanelType", model.panelType);
        vals.put("CompileTime", model.dayAndTime);
        vals.put("TestPatternMode", model.testPatternMode);
        vals.put("stPowerMode", model.stPowerMode);
        vals.put("DtvAvAbnormalDelay", model.dtvAvAbnormalDelay ? 1 : 0);
        vals.put("FactoryPreSetFeature", model.factoryPreset);
        vals.put("PanelSwing", model.panelSwingVal);
        vals.put("AudioPrescale", model.audioPreScale);
//      vals.put("ThreeDSelfAdaptieveLevel", model.st3DSelfAdaptiveLevel);
        vals.put("vdDspVersion", model.vdDspVersion);
        vals.put("eHidevMode", model.eHidevMode);
        vals.put("audioNrThr", model.audioNrThr);
        vals.put("audioSifThreshold", model.audioSifThreshold);
        vals.put("audioDspVersion", model.audioDspVersion);
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.factory/factoryextern"), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_FactoryExtern ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_FactoryExtern_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * for user setting audio prescale
     *
     * @param sourceId
     * @return
     */
    public int queryAudioPrescale()
    {
        String audioPrescale = new String("0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0, 0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0, 0x0,0x0,0x0,0x0,0x0,0x0,");
        int value = 0;
        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.usersetting/soundsetting"),
                null, null, null, null);
        if (cursor.moveToFirst())
        {
            audioPrescale = cursor.getString(cursor.getColumnIndex("SpeakerPreScale"));
            String[] array = audioPrescale.split(",");
            int iSource = queryCurInputSrc();
            String str = array[iSource];
//          String substr = str.substring(2);   // ignore "0x"
//          int prevalue = Integer.parseInt(substr, 16);
            // for auto switch radix 16 or 10
            String substr = str.substring(0);
            int prevalue = 0;
            if (substr.contains("0x"))
            {
                substr = substr.substring(2);
                prevalue = Integer.parseInt(substr, 16);
            }
            else
                prevalue = Integer.parseInt(substr, 10);
            value = prevalue/10*16+prevalue%10;
        }
        cursor.close();
        return value;
    }
    public int queryLastVideoStandardMode(int inputSrcType)
    {
        int value = -1;
        Cursor cursorVideo = getContentResolver().query(
                Uri.parse("content://mstar.tv.usersetting/videosetting/inputsrc/" + inputSrcType), null, null, null,
                null);
        if (cursorVideo.moveToNext())
        {
            value = cursorVideo.getInt(cursorVideo.getColumnIndex("LastVideoStandardMode"));
        }
        cursorVideo.close();
        return value;
    }

    public int queryArcMode(int inputSrcType)
    {
        int value = -1;
        Cursor cursor = getContentResolver().query(
                Uri.parse("content://mstar.tv.usersetting/videosetting/inputsrc/" + inputSrcType), null, null, null,
                null);
        if (cursor.moveToFirst())
        {
            value = cursor.getInt(cursor.getColumnIndex("enARCType"));

        }
        cursor.close();
        return value;
    }
    // for OverscanAdjust
    public ST_MAPI_VIDEO_WINDOW_INFO[][] queryOverscanAdjusts(int FactoryOverScanType)
    {
        ST_MAPI_VIDEO_WINDOW_INFO[][] model;
        switch (FactoryOverScanType)
        {
            // ST_MAPI_VIDEO_WINDOW_INFO m_DTVOverscanSet;//DTV's primary key is 0 in DB
            case 0:
                Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.factory/dtvoverscansetting"),
                        null, null, null, "ResolutionTypeNum");
                int maxDTV1 = MAX_DTV_Resolution_Info.E_DTV_MAX.ordinal();
                int maxDTV2 = MAPI_VIDEO_ARC_Type.E_AR_MAX.ordinal();
                model = new ST_MAPI_VIDEO_WINDOW_INFO[maxDTV1][maxDTV2];
                for (int i = 0; i < maxDTV1; i++)
                {
                    for (int j = 0; j < maxDTV2; j++)
                    {
                        ST_MAPI_VIDEO_WINDOW_INFO item = new ST_MAPI_VIDEO_WINDOW_INFO();
                        if (cursor.moveToNext())
                        {
                            item.u16H_CapStart = cursor
                                    .getInt(cursor.getColumnIndex("u16H_CapStart"));
                            item.u16V_CapStart = cursor
                                    .getInt(cursor.getColumnIndex("u16V_CapStart"));
                            item.u8HCrop_Left = (short) cursor.getInt(cursor
                                    .getColumnIndex("u8HCrop_Left"));
                            item.u8HCrop_Right = (short) cursor.getInt(cursor
                                    .getColumnIndex("u8HCrop_Right"));
                            item.u8VCrop_Up = (short) cursor.getInt(cursor
                                    .getColumnIndex("u8VCrop_Up"));
                            item.u8VCrop_Down = (short) cursor.getInt(cursor
                                    .getColumnIndex("u8VCrop_Down"));
                        }
                        model[i][j] = item;
                    }
                }
                cursor.close();
                break;
            // ST_MAPI_VIDEO_WINDOW_INFO m_HDMIOverscanSet;//HDMI's primary key is 1
            case 1:
                Cursor cursor1 = getContentResolver().query(Uri.parse("content://mstar.tv.factory/hdmioverscansetting"),
                        null, null, null, "ResolutionTypeNum");
                int maxHDMI1 = MAX_HDMI_Resolution_Info.E_HDMI_MAX.ordinal();
                int maxHDMI2 = MAPI_VIDEO_ARC_Type.E_AR_MAX.ordinal();
                model = new ST_MAPI_VIDEO_WINDOW_INFO[maxHDMI1][maxHDMI2];
                for (int i = 0; i < maxHDMI1; i++)
                {
                    for (int j = 0; j < maxHDMI2; j++)
                    {
                        ST_MAPI_VIDEO_WINDOW_INFO item = new ST_MAPI_VIDEO_WINDOW_INFO();
                        if (cursor1.moveToNext())
                        {

                            item.u16H_CapStart = cursor1.getInt(cursor1
                                    .getColumnIndex("u16H_CapStart"));
                            item.u16V_CapStart = cursor1.getInt(cursor1
                                    .getColumnIndex("u16V_CapStart"));
                            item.u8HCrop_Left = (short) cursor1.getInt(cursor1
                                    .getColumnIndex("u8HCrop_Left"));
                            item.u8HCrop_Right = (short) cursor1.getInt(cursor1
                                    .getColumnIndex("u8HCrop_Right"));
                            item.u8VCrop_Up = (short) cursor1.getInt(cursor1
                                    .getColumnIndex("u8VCrop_Up"));
                            item.u8VCrop_Down = (short) cursor1.getInt(cursor1
                                    .getColumnIndex("u8VCrop_Down"));

                        }
                        model[i][j] = item;
                    }
                }
                cursor1.close();
                break;
            // ST_MAPI_VIDEO_WINDOW_INFO m_YPbPrOverscanSet;//YPbPr's primary key is 2
            case 2:
                Cursor cursor2 = getContentResolver().query(Uri.parse("content://mstar.tv.factory/ypbproverscansetting"),
                        null, null, null, "ResolutionTypeNum");
                int maxYPbPr1 = MAX_YPbPr_Resolution_Info.E_YPbPr_MAX.ordinal();
                int maxYPbPr2 = MAPI_VIDEO_ARC_Type.E_AR_MAX.ordinal();
                model = new ST_MAPI_VIDEO_WINDOW_INFO[maxYPbPr1][maxYPbPr2];
                for (int i = 0; i < maxYPbPr1; i++)
                {
                    for (int j = 0; j < maxYPbPr2; j++)
                    {
                        ST_MAPI_VIDEO_WINDOW_INFO item = new ST_MAPI_VIDEO_WINDOW_INFO();
                        if (cursor2.moveToNext())
                        {

                            item.u16H_CapStart = cursor2.getInt(cursor2
                                    .getColumnIndex("u16H_CapStart"));
                            item.u16V_CapStart = cursor2.getInt(cursor2
                                    .getColumnIndex("u16V_CapStart"));
                            item.u8HCrop_Left = (short) cursor2.getInt(cursor2
                                    .getColumnIndex("u8HCrop_Left"));
                            item.u8HCrop_Right = (short) cursor2.getInt(cursor2
                                    .getColumnIndex("u8HCrop_Right"));
                            item.u8VCrop_Up = (short) cursor2.getInt(cursor2
                                    .getColumnIndex("u8VCrop_Up"));
                            item.u8VCrop_Down = (short) cursor2.getInt(cursor2
                                    .getColumnIndex("u8VCrop_Down"));

                        }
                        model[i][j] = item;
                    }
                }
                cursor2.close();
                break;
            // ST_MAPI_VIDEO_WINDOW_INFO m_VDOverscanSet;//VD's primary key is 3 in DB
            case 3:
                Cursor cursor3 = getContentResolver().query(Uri.parse("content://mstar.tv.factory/overscanadjust"),
                        null, null, null, "FactoryOverScanType");
                int maxVD1 = EN_VD_SIGNALTYPE.SIG_NUMS.ordinal();
                int maxVD2 = MAPI_VIDEO_ARC_Type.E_AR_MAX.ordinal();
                model = new ST_MAPI_VIDEO_WINDOW_INFO[maxVD1][maxVD2];
                for (int i = 0; i < maxVD1; i++)
                {
                    for (int j = 0; j < maxVD2; j++)
                    {
                        ST_MAPI_VIDEO_WINDOW_INFO item = new ST_MAPI_VIDEO_WINDOW_INFO();
                        if (cursor3.moveToNext())
                        {
                            item.u16H_CapStart = cursor3.getInt(cursor3
                                    .getColumnIndex("u16H_CapStart"));
                            item.u16V_CapStart = cursor3.getInt(cursor3
                                    .getColumnIndex("u16V_CapStart"));
                            item.u8HCrop_Left = (short) cursor3.getInt(cursor3
                                    .getColumnIndex("u8HCrop_Left"));
                            item.u8HCrop_Right = (short) cursor3.getInt(cursor3
                                    .getColumnIndex("u8HCrop_Right"));
                            item.u8VCrop_Up = (short) cursor3.getInt(cursor3
                                    .getColumnIndex("u8VCrop_Up"));
                            item.u8VCrop_Down = (short) cursor3.getInt(cursor3
                                    .getColumnIndex("u8VCrop_Down"));
                        }
                        model[i][j] = item;
                    }
                }
                cursor3.close();
                break;
            case 4:// ST_MAPI_VIDEO_WINDOW_INFO m_VDOverscanSet;//ATV primary key is 4 in DB
                Cursor cursor4 = getContentResolver().query(Uri.parse("content://mstar.tv.factory/atvoverscansetting"),
                        null, null, null, "ResolutionTypeNum");

                int maxVD3 = EN_VD_SIGNALTYPE.SIG_NUMS.ordinal();
                int maxVD4 = MAPI_VIDEO_ARC_Type.E_AR_MAX.ordinal();
                model = new ST_MAPI_VIDEO_WINDOW_INFO[maxVD3][maxVD4];
                for (int i = 0; i < maxVD3; i++)
                {
                    for (int j = 0; j < maxVD4; j++)
                    {
                        ST_MAPI_VIDEO_WINDOW_INFO item = new ST_MAPI_VIDEO_WINDOW_INFO();
                        if (cursor4.moveToNext())
                        {
                            item.u16H_CapStart = cursor4.getInt(cursor4
                                    .getColumnIndex("u16H_CapStart"));
                            item.u16V_CapStart = cursor4.getInt(cursor4
                                    .getColumnIndex("u16V_CapStart"));
                            item.u8HCrop_Left = (short) cursor4.getInt(cursor4
                                    .getColumnIndex("u8HCrop_Left"));
                            item.u8HCrop_Right = (short) cursor4.getInt(cursor4
                                    .getColumnIndex("u8HCrop_Right"));
                            item.u8VCrop_Up = (short) cursor4.getInt(cursor4
                                    .getColumnIndex("u8VCrop_Up"));
                            item.u8VCrop_Down = (short) cursor4.getInt(cursor4
                                    .getColumnIndex("u8VCrop_Down"));
                        }
                        model[i][j] = item;
                    }
                }
                cursor4.close();
                break;
            default:
                return null;
        }
        return model;
    }

    public void updateOverscanAdjust(int FactoryOverScanType, int arcMode, ST_MAPI_VIDEO_WINDOW_INFO[][] model)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();

        vals.put("u16H_CapStart", model[FactoryOverScanType][arcMode].u16H_CapStart);
        vals.put("u16V_CapStart", model[FactoryOverScanType][arcMode].u16V_CapStart);
        vals.put("u8HCrop_Left", model[FactoryOverScanType][arcMode].u8HCrop_Left);
        vals.put("u8HCrop_Right", model[FactoryOverScanType][arcMode].u8HCrop_Right);
        vals.put("u8VCrop_Up", model[FactoryOverScanType][arcMode].u8VCrop_Up);
        vals.put("u8VCrop_Down", model[FactoryOverScanType][arcMode].u8VCrop_Down);
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.factory/overscanadjust/factoryoverscantype/" + FactoryOverScanType
                        + "/_id/" + arcMode), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_OverscanAdjust ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_OverscanAdjust_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void updateYPbPrOverscanAdjust(int FactoryOverScanType, int arcMode, ST_MAPI_VIDEO_WINDOW_INFO[][] model)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();

        vals.put("u16H_CapStart", model[FactoryOverScanType][arcMode].u16H_CapStart);
        vals.put("u16V_CapStart", model[FactoryOverScanType][arcMode].u16V_CapStart);
        vals.put("u8HCrop_Left", model[FactoryOverScanType][arcMode].u8HCrop_Left);
        vals.put("u8HCrop_Right", model[FactoryOverScanType][arcMode].u8HCrop_Right);
        vals.put("u8VCrop_Up", model[FactoryOverScanType][arcMode].u8VCrop_Up);
        vals.put("u8VCrop_Down", model[FactoryOverScanType][arcMode].u8VCrop_Down);
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.factory/ypbproverscansetting/resolutiontypenum/" + FactoryOverScanType
                        + "/_id/" + arcMode), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_YPbPrOverscanSetting ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_YPbPrOverscanSetting_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void updateHDMIOverscanAdjust(int FactoryOverScanType, int arcMode, ST_MAPI_VIDEO_WINDOW_INFO[][] model)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();

        vals.put("u16H_CapStart", model[FactoryOverScanType][arcMode].u16H_CapStart);
        vals.put("u16V_CapStart", model[FactoryOverScanType][arcMode].u16V_CapStart);
        vals.put("u8HCrop_Left", model[FactoryOverScanType][arcMode].u8HCrop_Left);
        vals.put("u8HCrop_Right", model[FactoryOverScanType][arcMode].u8HCrop_Right);
        vals.put("u8VCrop_Up", model[FactoryOverScanType][arcMode].u8VCrop_Up);
        vals.put("u8VCrop_Down", model[FactoryOverScanType][arcMode].u8VCrop_Down);
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.factory/hdmioverscansetting/resolutiontypenum/" + FactoryOverScanType
                        + "/_id/" + arcMode), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_HDMIOverscanSetting ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_HDMIOverscanSetting_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void updateDTVOverscanAdjust(int FactoryOverScanType, int arcMode, ST_MAPI_VIDEO_WINDOW_INFO[][] model)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();

        vals.put("u16H_CapStart", model[FactoryOverScanType][arcMode].u16H_CapStart);
        vals.put("u16V_CapStart", model[FactoryOverScanType][arcMode].u16V_CapStart);
        vals.put("u8HCrop_Left", model[FactoryOverScanType][arcMode].u8HCrop_Left);
        vals.put("u8HCrop_Right", model[FactoryOverScanType][arcMode].u8HCrop_Right);
        vals.put("u8VCrop_Up", model[FactoryOverScanType][arcMode].u8VCrop_Up);
        vals.put("u8VCrop_Down", model[FactoryOverScanType][arcMode].u8VCrop_Down);
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.factory/dtvoverscansetting/resolutiontypenum/" + FactoryOverScanType
                        + "/_id/" + arcMode), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_DTVOverscanSetting ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_DTVOverscanSetting_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void updateATVOverscanAdjust(int FactoryOverScanType, int arcMode, ST_MAPI_VIDEO_WINDOW_INFO[][] model)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();

        vals.put("u16H_CapStart", model[FactoryOverScanType][arcMode].u16H_CapStart);
        vals.put("u16V_CapStart", model[FactoryOverScanType][arcMode].u16V_CapStart);
        vals.put("u8HCrop_Left", model[FactoryOverScanType][arcMode].u8HCrop_Left);
        vals.put("u8HCrop_Right", model[FactoryOverScanType][arcMode].u8HCrop_Right);
        vals.put("u8VCrop_Up", model[FactoryOverScanType][arcMode].u8VCrop_Up);
        vals.put("u8VCrop_Down", model[FactoryOverScanType][arcMode].u8VCrop_Down);

        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.factory/atvoverscansetting/resolutiontypenum/" + FactoryOverScanType
                        + "/_id/" + arcMode), vals, null, null);

        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_ATVOverscanSetting ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_ATVOverscanSetting_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * for PEQAdjust
     *
     * @param sourceId
     * @return
     */
    public AUDIO_PEQ_PARAM queryPEQAdjust(int index)
    {
        AUDIO_PEQ_PARAM model = new AUDIO_PEQ_PARAM();
        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.factory/peqadjust/" + index),
                null, null, null, null);
        if (cursor.moveToFirst())
        {
            model.Band = cursor.getInt(cursor.getColumnIndex("Band"));
            model.Gain = cursor.getInt(cursor.getColumnIndex("Gain"));
            model.Foh = cursor.getInt(cursor.getColumnIndex("Foh"));
            model.Fol = cursor.getInt(cursor.getColumnIndex("Fol"));
            model.QValue = cursor.getInt(cursor.getColumnIndex("QValue"));
        }
        cursor.close();
        return model;
    }

    public ST_FACTORY_PEQ_SETTING queryPEQAdjusts()
    {
        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.factory/peqadjust"),
                null, null, null, null);
        ST_FACTORY_PEQ_SETTING model = new ST_FACTORY_PEQ_SETTING();
        int i = 0;
        int length = model.stPEQParam.length;
        while (cursor.moveToNext())
        {
            if (i > length - 1)
            {
                break;
            }
            model.stPEQParam[i].Band = cursor.getInt(cursor.getColumnIndex("Band"));
            model.stPEQParam[i].Gain = cursor.getInt(cursor.getColumnIndex("Gain"));
            model.stPEQParam[i].Foh = cursor.getInt(cursor.getColumnIndex("Foh"));
            model.stPEQParam[i].Fol = cursor.getInt(cursor.getColumnIndex("Fol"));
            model.stPEQParam[i].QValue = cursor.getInt(cursor.getColumnIndex("QValue"));
            i++;
        }
        cursor.close();
        return model;
    }

    public void updatePEQAdjust(AUDIO_PEQ_PARAM model, int index)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("Band", model.Band);
        vals.put("Gain", model.Gain);
        vals.put("Foh", model.Foh);
        vals.put("Fol", model.Fol);
        vals.put("QValue", model.QValue);
        try
        {
            ret = getContentResolver().update(Uri.parse("content://mstar.tv.factory/peqadjust/" + index),
                    vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_PEQAdjust ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_PEQAdjust_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * for SSCAdjust
     *
     * @param sourceId
     * @return
     */
    public MS_FACTORY_SSC_SET querySSCAdjust()
    {
        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.factory/sscadjust"),
                null, null, null, null);
        MS_FACTORY_SSC_SET model = new MS_FACTORY_SSC_SET();
        if (cursor.moveToFirst())
        {
            model.Miu_SscEnable = cursor.getInt(cursor.getColumnIndex("Miu_SscEnable")) == 0 ? false : true;
            model.Lvds_SscEnable = cursor.getInt(cursor.getColumnIndex("Lvds_SscEnable")) == 0 ? false : true;
            model.Lvds_SscSpan = cursor.getInt(cursor.getColumnIndex("Lvds_SscSpan"));
            model.Lvds_SscStep = cursor.getInt(cursor.getColumnIndex("Lvds_SscStep"));
            model.Miu0_SscSpan = cursor.getInt(cursor.getColumnIndex("Miu_SscSpan"));
            model.Miu0_SscStep = cursor.getInt(cursor.getColumnIndex("Miu_SscStep"));
            model.Miu1_SscSpan = cursor.getInt(cursor.getColumnIndex("Miu1_SscSpan"));
            model.Miu1_SscStep = cursor.getInt(cursor.getColumnIndex("Miu1_SscStep"));
            model.Miu2_SscSpan = cursor.getInt(cursor.getColumnIndex("Miu2_SscSpan"));
            model.Miu2_SscStep = cursor.getInt(cursor.getColumnIndex("Miu2_SscStep"));
        }
        cursor.close();
        return model;
    }

    public void updateSSCAdjust(MS_FACTORY_SSC_SET model)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("Miu_SscEnable", model.Miu_SscEnable ? 1 : 0);
        vals.put("Lvds_SscEnable", model.Lvds_SscEnable ? 1 : 0);
        vals.put("Lvds_SscSpan", model.Lvds_SscSpan);
        vals.put("Lvds_SscStep", model.Lvds_SscStep);
        vals.put("Miu_SscSpan", model.Miu0_SscSpan);
        vals.put("Miu_SscStep", model.Miu0_SscStep);
        vals.put("Miu1_SscSpan", model.Miu1_SscSpan);
        vals.put("Miu1_SscStep", model.Miu1_SscStep);
        vals.put("Miu2_SscSpan", model.Miu2_SscSpan);
        vals.put("Miu2_SscStep", model.Miu2_SscStep);
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.factory/sscadjust"), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_SSCAdjust ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_SSCAdjust_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int queryCurInputSrc(){
        int value = 0;
        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.usersetting/systemsetting"), null,
                null, null, null);
        if (cursor.moveToFirst())
        {
            value = cursor.getInt(cursor.getColumnIndex("enInputSourceType"));
        }
        cursor.close();
        return value;
    }
    public int queryePicMode(int inputSrcType){
        Cursor cursorVideo = getContentResolver().query(
                Uri.parse("content://mstar.tv.usersetting/videosetting/inputsrc/" + inputSrcType), null, null, null,
                null);
        int value = -1;
        if (cursorVideo.moveToNext())
        {
            value = cursorVideo.getInt(cursorVideo.getColumnIndex("ePicture"));
        }
        cursorVideo.close();
        return value;
    }

    public void updatePictureMode(int ePicMode,int inputSrcType)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("ePicture",ePicMode );
        try
        {
            ret = getContentResolver()
                    .update(Uri.parse("content://mstar.tv.usersetting/videosetting/inputsrc/" + inputSrcType), vals,
                            null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_VideoSetting field ePicture ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_VideoSetting_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int queryColorTmpIdx(int inputSrcType,int ePicture){
        Cursor cursorPicMode = getContentResolver().query(
                Uri.parse("content://mstar.tv.usersetting/picmode_setting/inputsrc/" + inputSrcType + "/picmode/"
                        + ePicture), null, null, null, null);
        int value = -1;
        if (cursorPicMode.moveToNext())
        {
            value = cursorPicMode.getInt(cursorPicMode
                    .getColumnIndex("eColorTemp"));
        }
        cursorPicMode.close();
        return value;
    }


    public void updateColorTempIdx(int inputSrcType, int pictureModeType, EN_MS_COLOR_TEMP eColorTemp){
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("eColorTemp", eColorTemp.ordinal());
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.usersetting/picmode_setting/inputsrc/" + inputSrcType + "/picmode/"
                            + pictureModeType), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_PicMode_Setting field eColorTemp ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_PicMode_Setting_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * for factory White balance (Color tempreture data)
     *
     * @param sourceId
     * @return
     */
    public T_MS_COLOR_TEMP_DATA queryFactoryColorTempData(int colorTmpId)
    {

        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.factory/factorycolortemp/colortemperatureid/" + colorTmpId),
                            null, null, null, null);
        T_MS_COLOR_TEMP_DATA model = new T_MS_COLOR_TEMP_DATA((short)0, (short)0, (short)0, (short)0, (short)0, (short)0);
        if (cursor.moveToNext())
        {

            model.redgain = (short) cursor.getInt(cursor.getColumnIndex("u8RedGain"));
            model.greengain = (short) cursor
                    .getInt(cursor.getColumnIndex("u8GreenGain"));
            model.bluegain = (short) cursor.getInt(cursor.getColumnIndex("u8BlueGain"));
            model.redoffset = (short) cursor
                    .getInt(cursor.getColumnIndex("u8RedOffset"));
            model.greenoffset = (short) cursor.getInt(cursor
                    .getColumnIndex("u8GreenOffset"));
            model.blueoffset = (short) cursor.getInt(cursor
                    .getColumnIndex("u8BlueOffset"));
        }
        cursor.close();
        return model;
    }

    public void updateFactoryColorTempData(T_MS_COLOR_TEMP_DATA model, int colorTmpId)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("u8RedGain", model.redgain);
        vals.put("u8GreenGain", model.greengain);
        vals.put("u8BlueGain", model.bluegain);
        vals.put("u8RedOffset", model.redoffset);
        vals.put("u8GreenOffset", model.greenoffset);
        vals.put("u8BlueOffset", model.blueoffset);
        try
        {
            ret = getContentResolver().update(Uri.parse("content://mstar.tv.factory/factorycolortemp/colortemperatureid/" + colorTmpId),
                    vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_FactoryColorTemp ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_FacrotyColorTemp_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * for White balance (Color tempreture extern data)
     *
     * @param sourceId
     * @return
     */
    public T_MS_COLOR_TEMPEX queryFactoryColorTempExData()
    {
        T_MS_COLOR_TEMPEX model = new T_MS_COLOR_TEMPEX();
        for(int sourceIdx = 0; sourceIdx < EN_MS_COLOR_TEMP_INPUT_SOURCE.E_INPUT_SOURCE_NUM.ordinal();sourceIdx++){
            Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.factory/factorycolortempex"),
                    null, "InputSourceID = " + sourceIdx, null, "ColorTemperatureID");
            for (int colorTmpIdx = 0; colorTmpIdx < EN_MS_COLOR_TEMP.MS_COLOR_TEMP_NUM.ordinal(); colorTmpIdx++)
            {
                if (cursor.moveToNext())
                {
                    model.astColorTempEx[colorTmpIdx][sourceIdx].redgain =  cursor.getInt(cursor.getColumnIndex("u16RedGain"));
                    model.astColorTempEx[colorTmpIdx][sourceIdx].greengain =  cursor
                            .getInt(cursor.getColumnIndex("u16GreenGain"));
                    model.astColorTempEx[colorTmpIdx][sourceIdx].bluegain = cursor.getInt(cursor.getColumnIndex("u16BlueGain"));
                    model.astColorTempEx[colorTmpIdx][sourceIdx].redoffset = cursor
                            .getInt(cursor.getColumnIndex("u16RedOffset"));
                    model.astColorTempEx[colorTmpIdx][sourceIdx].greenoffset = cursor.getInt(cursor
                            .getColumnIndex("u16GreenOffset"));
                    model.astColorTempEx[colorTmpIdx][sourceIdx].blueoffset = cursor.getInt(cursor
                            .getColumnIndex("u16BlueOffset"));
                }
            }
            cursor.close();
        }
        return model;
    }

    public void updateFactoryColorTempExData(T_MS_COLOR_TEMPEX_DATA model, int sourceId, int colorTmpId)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("u16RedGain", model.redgain);
        vals.put("u16GreenGain", model.greengain);
        vals.put("u16BlueGain", model.bluegain);
        vals.put("u16RedOffset", model.redoffset);
        vals.put("u16GreenOffset", model.greenoffset);
        vals.put("u16BlueOffset", model.blueoffset);
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.factory/factorycolortempex/inputsourceid/" + sourceId
                        + "/colortemperatureid/" + colorTmpId), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_FactoryColorTempEx ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_FacrotyColorTempEx_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public enum USER_SETTING_FIELD{
        bEnableWDT,
        bUartBus,

    }
    public int queryUserSysSetting(USER_SETTING_FIELD field)
    {
        String fieldStr = "";
        switch(field){
        case bEnableWDT:
            fieldStr = "bEnableWDT";
            break;
        case bUartBus:
            fieldStr = "bUartBus";
            break;
        default:
            return -1;
        }
        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.usersetting/systemsetting"),
                null, null, null, null);
        int value = -1;
        if (cursor.moveToFirst())
        {
            value = cursor.getInt(cursor.getColumnIndex(fieldStr));
        }
        cursor.close();
        return value;
    }

    public void updateUserSysSetting(USER_SETTING_FIELD field,int value){
        long ret = -1;
        String fieldStr = "";
        switch(field){
        case bEnableWDT:
            fieldStr = "bEnableWDT";
            break;
        case bUartBus:
            fieldStr = "bUartBus";
            break;
        default:
            return ;
        }
        ContentValues vals = new ContentValues();
        vals.put(fieldStr, value);
        try
        {
            ret = getContentResolver().update(Uri.parse("content://mstar.tv.usersetting/systemsetting"),
                    vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_SystemSetting field bEnableWDT or bUartBus ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_SystemSetting_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int queryEnableSTR() {
        Cursor cursor = getContentResolver().query(Uri.parse("content://mstar.tv.usersetting/systemsetting"),
                null, null, null, null);
        int value = -1;
        if (cursor.moveToFirst())
        {
            value = cursor.getInt(cursor.getColumnIndex("u32StrPowerMode"));
        }
        cursor.close();
        return value;
    }

    public void updateEnableSTR(int value){
        long ret = -1;
        ContentValues vals = new ContentValues();
        vals.put("u32StrPowerMode", value);
        try
        {
            ret = getContentResolver().update(Uri.parse("content://mstar.tv.usersetting/systemsetting"),
                    vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("updateEnableSTR failed");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_SystemSetting_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void updateSelfAdaptiveLevel(int value, int inputSrcType)
    {
//      ContentValues vals = new ContentValues();
//      vals.put("eThreeDVideoSelfAdaptiveLevel", value);
//      userSettingDB.update("tbl_ThreeDVideoMode", vals, "InputSrcType = " + inputSrcType, null);
//      try
//        {
//          TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_ThreeDVideoMode_IDX);
//        }
//        catch (TvCommonException e)
//        {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//        }
    }
    public int querySelfAdaptiveLevel( int inputSrcType){
        int value = 0;
//      Cursor cursor3DMode = userSettingDB.query("tbl_ThreeDVideoMode", null, "InputSrcType = " + inputSrcType, null, null, null,
//              null);
//      if (cursor3DMode.moveToFirst())
//      {
//          value = cursor3DMode.getInt(cursor3DMode .getColumnIndex("eThreeDVideoSelfAdaptiveLevel"));
//
//      }
//      cursor3DMode.close();
        return value;
    }
    public void updatePicModeSetting(EN_MS_VIDEOITEM eIndex, int inputSrcType, int pictureModeType, int value)
    {
        long ret = -1;
        ContentValues vals = new ContentValues();
        switch(eIndex){
        case MS_VIDEOITEM_BRIGHTNESS:
            vals.put("u8Brightness", value);
            break;
        case MS_VIDEOITEM_CONTRAST:
            vals.put("u8Contrast",value);
            break;
        case MS_VIDEOITEM_HUE:
            vals.put("u8Hue", value);
            break;
        case MS_VIDEOITEM_SATURATION:
            vals.put("u8Saturation", value);
            break;
        case MS_VIDEOITEM_SHARPNESS:
            vals.put("u8Sharpness", value);
            break;
        case MS_VIDEOITEM_BACKLIGHT:
            vals.put("u8Backlight", value);
            break;
            default:
                break;
        }
        try
        {
            ret = getContentResolver().update(
                    Uri.parse("content://mstar.tv.usersetting/picmode_setting/inputsrc/" + inputSrcType + "/picmode/"
                            + pictureModeType), vals, null, null);
        }
        catch(SQLException e)
        {
        }
        if(ret == -1)
        {
            System.out.println("update tbl_PicMode_Setting ignored");
        }

        try
        {
            TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_PicMode_Setting_IDX);
        }
        catch (TvCommonException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int queryPicModeSetting(EN_MS_VIDEOITEM eIndex, int inputSrcType, int pictureModeType){
        Cursor cursorPicMode = getContentResolver().query(
                Uri.parse("content://mstar.tv.usersetting/picmode_setting/inputsrc/" + inputSrcType + "/picmode/"
                        + pictureModeType), null, null, null, null);
        cursorPicMode.moveToFirst();
        int value = 0;
        switch(eIndex){
        case MS_VIDEOITEM_BRIGHTNESS:
            value = cursorPicMode.getInt(cursorPicMode.getColumnIndex("u8Brightness"));
            break;
        case MS_VIDEOITEM_CONTRAST:
            value = cursorPicMode.getInt(cursorPicMode.getColumnIndex("u8Contrast"));
            break;
        case MS_VIDEOITEM_HUE:
            value = cursorPicMode.getInt(cursorPicMode.getColumnIndex("u8Hue"));
            break;
        case MS_VIDEOITEM_SATURATION:
            value = cursorPicMode.getInt(cursorPicMode.getColumnIndex("u8Saturation"));
            break;
        case MS_VIDEOITEM_SHARPNESS:
            value = cursorPicMode.getInt(cursorPicMode.getColumnIndex("u8Sharpness"));
            break;
        case MS_VIDEOITEM_BACKLIGHT:
            value = cursorPicMode.getInt(cursorPicMode.getColumnIndex("u8Backlight"));
            break;
            default:
                break;
        }
        cursorPicMode.close();
        return value;
    }
	// DB access control for MS_USER_SYSTEM_SETTING stUsrData;
	public short queryEnergyEfficiencyBacklight()  {
		short u8EnergyEfficiencyBacklight = 0;
		Cursor cursor = getContentResolver().query(
				Uri.parse("content://mstar.tv.usersetting/systemsetting"),
				null, null, null, null);
		if (cursor.moveToFirst()) {
			u8EnergyEfficiencyBacklight = (short)cursor.getInt(cursor.getColumnIndex("u8EnergyEfficiencyBacklight"));
		}
		cursor.close();
		return u8EnergyEfficiencyBacklight;
	}
	
	// DB access control for MS_USER_SYSTEM_SETTING stUsrData;
	public short queryEnergyEfficiencyBacklightmax()  {
		short u8EnergyEfficiencyBacklight_max = 0;
		Cursor cursor = getContentResolver().query(
				Uri.parse("content://mstar.tv.usersetting/systemsetting"),
				null, null, null, null);
		if (cursor.moveToFirst()) {
			u8EnergyEfficiencyBacklight_max = (short)cursor.getInt(cursor.getColumnIndex("u8EnergyEfficiencyBacklight_Max"));
		}
		cursor.close();
		return u8EnergyEfficiencyBacklight_max;
	}
	
	public void updateEnergyEfficiencyBacklight(short u8EnergyEfficiencyBacklight) {
		int ret = -1;
		ContentValues vals = new ContentValues();
		vals.put("u8EnergyEfficiencyBacklight", u8EnergyEfficiencyBacklight);
		vals.put("BackupEnergyBacklight", u8EnergyEfficiencyBacklight);
		try {
			ret = getContentResolver().update(
					Uri.parse("content://mstar.tv.usersetting/systemsetting"),
					vals, null, null);
		} catch (SQLException e) {
		}
		if (ret == -1) {
			System.out.println("update tbl_SystemSetting ignored");
		}
		try {
			TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_SystemSetting_IDX);
		} catch (TvCommonException e) {
			e.printStackTrace();
		}
	}
	
	public void updateEnergyEfficiencyBacklightmax(short u8EnergyEfficiencyBacklight) {
		int ret = -1;
		ContentValues vals = new ContentValues();
		vals.put("u8EnergyEfficiencyBacklight_Max", u8EnergyEfficiencyBacklight);
		vals.put("BackupEnergyBacklightMax", u8EnergyEfficiencyBacklight);
		try {
			ret = getContentResolver().update(
					Uri.parse("content://mstar.tv.usersetting/systemsetting"),
					vals, null, null);
		} catch (SQLException e) {
		}
		if (ret == -1) {
			System.out.println("update tbl_SystemSetting ignored");
		}
		try {
			TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_SystemSetting_IDX);
		} catch (TvCommonException e) {
			e.printStackTrace();
		}
	}
	
	//====================age mode start
	public short getagemode()  {
		short bAgeModeFlag = 0;
		try {
		Cursor cursor = getContentResolver().query(
				Uri.parse("content://mstar.tv.usersetting/systemsetting"),
				null, null, null, null);
		if (cursor.moveToFirst()) {
			bAgeModeFlag = (short)cursor.getInt(cursor.getColumnIndex("bAgeModeFlag"));
		}
		cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bAgeModeFlag;
	}
	
	public void setAgemode(short bAgeModeFlag) {
		int ret = -1;
		ContentValues vals = new ContentValues();
		vals.put("bAgeModeFlag", bAgeModeFlag);
		try {
			ret = getContentResolver().update(
					Uri.parse("content://mstar.tv.usersetting/systemsetting"),
					vals, null, null);
		} catch (SQLException e) {
		}
		if (ret == -1) {
			System.out.println("update tbl_SystemSetting ignored");
		}
		try {
			TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_SystemSetting_IDX);
		} catch (TvCommonException e) {
			e.printStackTrace();
		}
	}
	//====================age mode end
	
	/**
	 * nathan.liao 20140409
	 * @return
	 */
	//====================ktc avc start
	public short getKtcavc()  {
		short bKtcavcflag = 0;
		Cursor cursor = getContentResolver().query(
				Uri.parse("content://mstar.tv.usersetting/systemsetting"),
				null, null, null, null);
		if (cursor.moveToFirst()) {
			bKtcavcflag = (short)cursor.getInt(cursor.getColumnIndex("bEnableAvc_ktc"));
		}
		cursor.close();
		return bKtcavcflag;
	}
	
	public void setKtcavc(int bKtcavcflag) {
		int ret = -1;
		ContentValues vals = new ContentValues();
		vals.put("bEnableAvc_ktc", bKtcavcflag);
		try {
			ret = getContentResolver().update(
					Uri.parse("content://mstar.tv.usersetting/systemsetting"),
					vals, null, null);
		} catch (SQLException e) {
		}
		if (ret == -1) {
			System.out.println("update tbl_SystemSetting ignored");
		}
		try {
			TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_SystemSetting_IDX);
		} catch (TvCommonException e) {
			e.printStackTrace();
		}
	}
	//====================ktc avc end
	
	public short getFactoryMode() {
		short bFactoryMode = 0;
		try {
			Cursor cursor = getContentResolver().query(
				Uri.parse("content://mstar.tv.usersetting/systemsetting"),
				null, null, null, null);
			if (cursor.moveToFirst()) {
				bFactoryMode = (short)cursor.getInt(cursor.getColumnIndex("FactoryDebugMode"));
			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bFactoryMode;
	}
	
	public void setFactoryMode(short factoryMode) {
		int ret = -1;
		ContentValues vals = new ContentValues();
		vals.put("FactoryDebugMode", factoryMode);
		try {
			ret = getContentResolver().update(
					Uri.parse("content://mstar.tv.usersetting/systemsetting"),
					vals, null, null);
		} catch (SQLException e) {
		}
		if (ret == -1) {
			System.out.println("update tbl_SystemSetting ignored");
		}
		try {
			TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_SystemSetting_IDX);
		} catch (TvCommonException e) {
			e.printStackTrace();
		}
	}
	
	public void setWbPattern(short wbPattern) {
		int ret = -1;
		ContentValues vals = new ContentValues();
		vals.put("WBWhitePattern", wbPattern);
		try {
			ret = getContentResolver().update(
					Uri.parse("content://mstar.tv.usersetting/systemsetting"),
					vals, null, null);
		} catch (SQLException e) {
		}
		if (ret == -1) {
			System.out.println("update tbl_SystemSetting ignored");
		}
		try {
			TvManager.getInstance().getDatabaseManager().setDatabaseDirtyByApplication(IFactoryDesk.T_SystemSetting_IDX);
		} catch (TvCommonException e) {
			e.printStackTrace();
		}
	}
}

package com.liushiyao.flickr;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class SearchExample
{
    static String apiKey = "自己的key";

    static String sharedSecret = "自己的secret";

    public static final int MAX_PRE_PAGE = 500;

    Flickr flickr;

    public SearchExample() throws IOException
    {

        flickr = new Flickr(apiKey, sharedSecret, new REST());

        Flickr.debugRequest = false;
        Flickr.debugStream = false;
    }

    private PhotoList<Photo> search(String text) throws FlickrException
    {
        PhotosInterface photos = flickr.getPhotosInterface();
        SearchParameters params = new SearchParameters();
        Set<String> strings = new HashSet<String>();
        strings.add("date_taken");
        strings.add("tags");
        strings.add("title");
        strings.add("geo");                 //获取经纬度信息，如果没有设置woeid则此项没有返回结果
        strings.add("owner_name");
        params.setExtras(strings);          //额外信息
        String [] tags = new String[] {"广州"};
        params.setTags(tags);
        params.setText(text);
        params.setWoeId("26198245");   //woeid-地点id,可以在https://www.flickr.com/places/info/26198245 查询
        params.setMinTakenDate(new Date(1093996800));
        params.setSort(SearchParameters.DATE_POSTED_DESC);
        PhotoList<Photo> photoPhotoList = new PhotoList<Photo>();
        int index = 1,pages = 1;
        do {
            PhotoList<Photo> results = photos.search(params, MAX_PRE_PAGE, index);
            if(results != null && !results.isEmpty()){
                photoPhotoList.addAll(results);
            }
            pages = results.getPages();//分页后共有多少页
            int page = results.getPage();
            System.out.println("进度:"+page+"/"+pages+"页");
            index++;
        }while (index < pages);

        return photoPhotoList;
    }

    public static void main(String[] args) throws Exception
    {
        SearchExample t = new SearchExample();
        PhotoList<Photo>  photoPhotoList= t.search(args.length == 0 ? "广州" : args[0]);
        System.out.println("总共:"+photoPhotoList.size()+"条数据");
        // 创建工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建工作表
        HSSFSheet sheet = workbook.createSheet("sheet1");
        //标题
        HSSFRow titleRows = sheet.createRow(0);
        titleRows.createCell(0).setCellValue("id");
        titleRows.createCell(1).setCellValue("用户名称");
        titleRows.createCell(2).setCellValue("图片标题");
        titleRows.createCell(3).setCellValue("拍摄时间");
        titleRows.createCell(4).setCellValue("标签");
        titleRows.createCell(5).setCellValue("图片链接");
        titleRows.createCell(6).setCellValue("经度");
        titleRows.createCell(7).setCellValue("纬度");

        for (int row = 0; row < photoPhotoList.size(); row++)
        {
            HSSFRow rows = sheet.createRow(row+1);
            // 向工作表中添加数据
            rows.createCell(0).setCellValue(photoPhotoList.get(row).getId());
            rows.createCell(1).setCellValue(photoPhotoList.get(row).getOwner().getUsername());
            rows.createCell(2).setCellValue(photoPhotoList.get(row).getTitle());
            rows.createCell(3).setCellValue(new SimpleDateFormat("yyy-MM-dd").format(photoPhotoList.get(row).getDateTaken()));
            rows.createCell(4).setCellValue(Arrays.toString(photoPhotoList.get(row).getTags().toArray()));
            rows.createCell(5).setCellValue(photoPhotoList.get(row).getLargeUrl());
            rows.createCell(6).setCellValue(photoPhotoList.get(row).getGeoData().getLongitude());
            rows.createCell(7).setCellValue(photoPhotoList.get(row).getGeoData().getLatitude());
        }
        sheet.createRow(photoPhotoList.size()+2).createCell(0).setCellValue("总数："+photoPhotoList.size()+"条");
        File xlsFile = new File("data.xls");
        FileOutputStream xlsStream = new FileOutputStream(xlsFile);
        workbook.write(xlsStream);
    }
}

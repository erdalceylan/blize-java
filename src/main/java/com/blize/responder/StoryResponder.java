package com.blize.responder;

import com.blize.document.result.StoryGroup;
import com.blize.document.result.StoryViewItem;
import com.blize.dto.response.*;
import com.blize.entity.User;
import com.blize.service.files.*;
import com.blize.repository.UserRepository;
import com.blize.service.MongoStoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StoryResponder {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MongoStoryService mongoStoryService;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private FileService fileService;
    @Autowired
    private LocalFileUploader localFileUploader;
    @Autowired
    private S3FileUploader s3FileUploader;

    public List<StoryGroupResponseDTO> groupList(User user, int offset) {

        var groupList = mongoStoryService.groupList(user, offset);

        var userIds = groupList.stream().map(StoryGroup::getFrom).toList();
        var users = userRepository.findUsersByIdIn(userIds);
        var userMap = users.stream().collect(Collectors.toMap(User::getId, usr -> usr));
        List<StoryGroupResponseDTO> groups = new ArrayList<>();

        for (StoryGroup storyGroup : groupList) {
            List<StoryGroupItemResponseDTO> itemResponses = storyGroup.getItems().stream()
                    .map(item -> {
                        var storyGroupItemResponse = new StoryGroupItemResponseDTO();
                        storyGroupItemResponse.setId(item.get_id());
                        storyGroupItemResponse.setDate(item.getDate());
                        storyGroupItemResponse.setFileName(item.getFileName());
                        storyGroupItemResponse.setPath(item.getPath());
                        storyGroupItemResponse.setRootPath(item.getRootPath());
                        storyGroupItemResponse.setSeen(item.getSeen());
                        return storyGroupItemResponse;
                    }).toList();

            var storyGroupResponse = new StoryGroupResponseDTO();
            storyGroupResponse.setUser(userMap.get(storyGroup.getFrom()));
            storyGroupResponse.setItems(itemResponses);
            groups.add(storyGroupResponse);
        }

        return groups;
    }

    public List<StoryMeItemResponseDTO> meList(User user) {

        var meList = mongoStoryService.meList(user);
        var userIds = meList.stream()
                .flatMap(meItem -> meItem.getViews().stream())
                .map(StoryViewItem::getFrom)
                .toList();

        var users = userRepository.findUsersByIdIn(userIds);
        var userMap = users.stream().collect(Collectors.toMap(User::getId, usr -> usr));

        return meList.stream().map(item -> {
            var itemMeItemResponse = new StoryMeItemResponseDTO();
            itemMeItemResponse.setId(item.get_id());
            itemMeItemResponse.setDate(item.getDate());
            itemMeItemResponse.setFileName(item.getFileName());
            itemMeItemResponse.setPath(item.getPath());
            itemMeItemResponse.setRootPath(item.getRootPath());
            itemMeItemResponse.setUser(user);
            itemMeItemResponse.setViewsLength(item.getViewsLength());
            itemMeItemResponse.setViews(
                    item.getViews().stream().map(itm -> {
                        var viewItemResponse = new StoryViewItemResponseDTO();
                        viewItemResponse.setDate(itm.getDate());
                        viewItemResponse.setUser(userMap.get(itm.getFrom()));
                        return viewItemResponse;
                    }).toList()
            );

            return itemMeItemResponse;
        }).toList();
    }

    public List<StoryViewItemResponseDTO> viewList(User user, String _id, Integer offset) {

        var viewList = mongoStoryService.viewList(user, _id, offset);
        List<StoryViewItem> views;
        if (viewList == null || viewList.isEmpty() || (views = viewList.get(0).getViews()).isEmpty()) {
            return new ArrayList<>();
        }

        var users = userRepository.findUsersByIdIn(views.stream().map(StoryViewItem::getFrom).toList());

        return views.stream().map(itm -> {
            var viewItemResponse = new StoryViewItemResponseDTO();
            viewItemResponse.setDate(itm.getDate());
            viewItemResponse.setUser(users.stream().filter(u -> u.getId() == itm.getFrom()).findAny().orElseThrow());
            return viewItemResponse;
        }).toList();
    }

    public Object add(User user, InputStreamSource file) {
        var activeUserStoryCount = mongoStoryService.activeStoryCount(user);

        if (activeUserStoryCount < MongoStoryService.USER_ITEM_LIMIT) {
            try {
                var rootFolder = "/files/images";
                var filePath = "/story/"+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
                var filename = DigestUtils.md5DigestAsHex((System.nanoTime() + UUID.randomUUID().toString()).getBytes())+".jpeg";
                BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

                imageUploadService.checkSize(bufferedImage, 288, 512);
                bufferedImage = imageUploadService.resizeMin(bufferedImage,576, 1024);
                bufferedImage = imageUploadService.cropRatio(bufferedImage, 9.0/16);
                //FileUploader.FileInfo fileInfo = imageUploadService.save(bufferedImage, rootFolder, filePath, filename, localFileUploader);
                FileUploader.FileInfo fileInfo = imageUploadService.save(bufferedImage, rootFolder, filePath, filename, s3FileUploader);
                var story = mongoStoryService.add(user, fileInfo.getRoot(), fileInfo.path(), fileInfo.fileName());

                var storyResponse = new StoryResponseDTO();
                storyResponse.setId(story.getId());
                storyResponse.setDate(story.getDate());
                storyResponse.setRootPath(story.getRootPath());
                storyResponse.setPath(story.getPath());
                storyResponse.setFileName(story.getFileName());
                storyResponse.setFrom(user);

                return storyResponse;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public Object seen(User user, String _id) {

        return mongoStoryService.seen(user, _id);
    }

    public Object delete(User user, String _id) {

        return mongoStoryService.delete(user, _id);
    }
}

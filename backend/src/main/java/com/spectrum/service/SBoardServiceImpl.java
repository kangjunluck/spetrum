package com.spectrum.service;

import com.spectrum.common.request.SBoardPutReq;
import com.spectrum.common.request.SBoardRegisterReq;
import com.spectrum.common.response.SBoardRes;
import com.spectrum.entity.SBoard;
import com.spectrum.entity.SBoardFile;
import com.spectrum.entity.SComment;
import com.spectrum.entity.User;
import com.spectrum.repository.SBoardFileRepository;
import com.spectrum.repository.SBoardRepository;
import com.spectrum.repository.SCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class SBoardServiceImpl implements SBoardService {
    @Autowired
    SBoardRepository sBoardRepository;
    @Autowired
    SBoardFileRepository sBoardFileRepository;
    @Autowired
    SCommentRepository sCommentRepository;
    @Autowired
    FileHandler fileHandler;

    String BASE_PATH = "/var/lib/jenkins/workspace/PJT/backend/src/main/resources/";
//    String BASE_PATH = new File("").getAbsolutePath() + "/src/main/resources/";

    @Override
    public List<SBoardRes> getSBoardsByUser(User user, Pageable pageable) {
        List<SBoardRes> res = new ArrayList<>();
        List<SBoard> sboards = sBoardRepository.findAllByUser(user, pageable);
        for (SBoard sBoard : sboards) {
            Optional<List<SBoardFile>> sboardf = sBoardFileRepository.findBysBoard(sBoard);
            System.out.println(sboardf.get());
            SBoardRes br = new SBoardRes();
            br.setFilelist(sboardf.get());
            br.setId(sBoard.getId());
            br.setUserid(sBoard.getUser().getUserId());
            br.setContent(sBoard.getContent());
            br.setCreated(sBoard.getCreated());
            br.setUpdated(sBoard.getUpdated());
            br.setLikes(sBoard.getLikes());
            br.setFilelist(sboardf.get());
            res.add(br);
        }
        return res;
    }



    @Override
    public SBoard createSBoard(User user, SBoardRegisterReq sboardinfo, List<MultipartFile> sboardfiles) throws IOException {
        SBoard sBoard = new SBoard();
        sBoard.setUser(user);
        String content = sboardinfo.getContent().replace("\r\n","<br>");
        sBoard.setContent(content);
        sBoard.setCreated(new Date());
        sBoard.setUpdated(new Date());
        sBoard.setLikes(0);
        SBoard tmpsboard =  sBoardRepository.save(sBoard);

        // ????????? ????????? ?????? ?????? ??????
        String path = BASE_PATH + "image/sns/" + user.getId() +'/'+ tmpsboard.getId();
        File file = new File(path);
        // ??????????????? ???????????? ?????? ??????
        if(!file.exists()) {
            boolean wasSuccessful = file.mkdirs();
            // ???????????? ????????? ???????????? ??????
            if(!wasSuccessful)
                System.out.println("file: was not successful");
        }
        file.setWritable(true);
        file.setReadable(true);

        // ?????? ????????????
        List<SBoardFile> photoList = fileHandler.parseFileInfo(sboardfiles, user, tmpsboard);
        System.out.println(photoList);
        // ????????? ????????? ????????? ??????
        if(!photoList.isEmpty()){
            for(SBoardFile photo : photoList)
                // ????????? DB??? ??????
                sBoardFileRepository.save(photo);
        }
        return sBoard;
    }

    @Override
    public SBoard getSBoardsById(Long sboardid) {
        return sBoardRepository.getById(sboardid);
    }

    @Override
    public SBoard putSBoard(User user, SBoardPutReq sboardinfo, Long sboardid) {
        SBoard sBoard = sBoardRepository.getById(sboardid);
        sBoard.setUser(user);
        String content = sboardinfo.getContent().replace("\n","<br>");
        sBoard.setContent(content);
        sBoard.setUpdated(new Date());
        SBoard tmpsboard =  sBoardRepository.save(sBoard);
        // ????????? ?????? ????????? ??????.
        return sBoard;
    }

    @Override
    public Boolean deleteSBoard(User user, Long sboardid) {
        Optional<SBoard> sb = sBoardRepository.findById(sboardid);
        if (sb == null) {
            return false;
        }
        Optional<List<SBoardFile>> photoList = sBoardFileRepository.findBysBoard(sb.get());
        fileHandler.deleteFile(user, sboardid, photoList);
        if(!photoList.get().isEmpty()){
            for(SBoardFile photo : photoList.get())
                // ????????? DB??? ??????
                sBoardFileRepository.delete(photo);
        }
        // ?????? ?????? ??? ??????
        Optional<List<SComment>> comments = sCommentRepository.findBysBoard(sb.get());
        if(!comments.get().isEmpty()){
            for(SComment scomment : comments.get()){
                sCommentRepository.delete(scomment);
            }
        }
        sBoardRepository.delete(sb.get());
        return true;
    }

    @Override
    public List<SBoardFile> getFilesBysBoard(SBoard sboard) {
        Optional<List<SBoardFile>> files = sBoardFileRepository.findBysBoard(sboard);
        return files.get();
    }

}

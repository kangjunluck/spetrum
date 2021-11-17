package com.spectrum.service;

import com.spectrum.common.jwt.JWTUtil;
import com.spectrum.common.request.DoggingPostReq;
import com.spectrum.common.response.DoggingDetailResponse;
import com.spectrum.entity.Dogging;
import com.spectrum.entity.User;
import com.spectrum.repository.DoggingRepository;
import com.spectrum.repository.UserRepository;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.lang.Object;


@Service
public class DoggingServiceImpl implements DoggingService{

    @Autowired
    private DoggingRepository doggingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtil jwtUtil;

    private EntityManager em;

    private static User user;
    private static String filePath = "src/main/resources/";
    String BASE_PATH = "/var/lib/jenkins/workspace/PJT/backend/src/main/resources/";

    public DoggingServiceImpl() {
    }

    @Override
    public void saveUserInfo(String token){
        String userid = jwtUtil.getUsername(token);
        Optional<User> userOptional = userRepository.findByUserId(userid);
        user = userOptional.get();
        return;
    }

    @Override
    public List<Dogging> MyDoggingList() throws IOException{
        return doggingRepository.findByUserId(user.getId());
    }
    @Override
    public DoggingDetailResponse DetailOfDogging(Long doggingId) throws IOException{
//        String fileName = "";
        //파일 이름 : userid+"_"+doggingId
        Object[] objects = GPX.read(filePath+user.getId()+"_"+doggingId+".gpx")
                .tracks()
                .flatMap(Track::segments)
                .flatMap(TrackSegment::points).toArray();

        Dogging dogging = doggingRepository.getById(doggingId);
        DoggingDetailResponse res = new DoggingDetailResponse();
        res.setDistance(dogging.getDistance());
        res.setTime(dogging.getTime());
        res.setDate(dogging.getDate());
        res.setLocation(dogging.getLocation());
        res.setPicture(dogging.getCustomPicturePath()); //얘를 줄일 수 있을 것 같은뎅
        res.setObjects(objects);

        return res;
    }

    @Override
    public void PostDogging(DoggingPostReq doggingPostReq)  {
        Date date = new Date();

        Dogging dogging = new Dogging();
        dogging.setDistance(doggingPostReq.getDistance());
        dogging.setTime(doggingPostReq.getTime());
        dogging.setLocation(doggingPostReq.getLocation());
        dogging.setDate(date);
        dogging.setUser(user);

        doggingRepository.save(dogging);
    }



    @Override
    public Dogging customImage(MultipartFile multipartFile, String userid) throws IOException{

        User user = userService.findUserByUserId(userid);
        Long userId = user.getId();

        Dogging dogging = doggingRepository.findFirstByUserIdOrderByDateDesc(userId);

        CustomImage customImage = new CustomImage();
        String path = customImage.customImage(multipartFile, dogging);
        dogging.setCustomPicturePath(path);
        doggingRepository.save(dogging);

        return dogging;
    }


    @Override
    public void SaveDoggingPath(Long userId, Long doggingId, float[] lats, float[] lngs) throws IOException{

        //GPX파일 안 만들고 MySQL 데이터 타입 중에 polygon으로 만들 수 있는지 보자~!
        String fileName = userId+"_"+doggingId;

        List<WayPoint> wayPoints = new ArrayList<>();

        for(int i=0; i<lats.length; i++){
            wayPoints.add(WayPoint.of(lats[i], lngs[i]));
        }
        final GPX gpx = GPX.builder()
                .addTrack(track -> track
                        .addSegment(segment -> segment
                                .points(wayPoints)))
                .build();

        GPX.write(gpx, filePath+"dogging/"+fileName+".gpx");
        return;
    }

    @Override
    public void DeleteDogging(Long id){
        doggingRepository.deleteById(id);
    }
}

package sys;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;
import cn.v1.system.pojo.SysOffice;
import cn.v1.system.service.SysOfficeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/11/1
 * @Description:
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:META-INF/spring/*.xml")
public class OfficeTest {

    @Autowired
    private SysOfficeService sysOfficeService;

    @Test
    public void getById(){
        SysOffice office = sysOfficeService.getById("1");
        System.out.printf(office.toString());

    }

    @Test
    public void update(){
        SysOffice office = sysOfficeService.getById("1");
        sysOfficeService.save(office);
    }

    @Test
    public void getList(){
      List<SysOffice> list = sysOfficeService.getList();
      for(SysOffice office : list){
          System.out.printf(list.size()+"");
      }
    }

    @Test
    public void getPage(){
        Page<SysOffice> page = sysOfficeService.getPage(new SysOffice(), new PageBounds(0,20));
        System.out.printf(page.getResult().size()+"");
    }



}

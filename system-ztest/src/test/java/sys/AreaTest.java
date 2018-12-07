package sys;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;
import cn.v1.system.pojo.SysArea;
import cn.v1.system.service.SysAreaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/11/1
 * @Description:
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:META-INF/spring/*.xml")
public class AreaTest {

    @Autowired
    private SysAreaService sysAreaService;

    @Test
    public void getById(){
        SysArea area = sysAreaService.getById("1009");
        System.out.printf(area.toString());

    }

    @Test
    public void update(){
        SysArea area = sysAreaService.getById("1009");
        sysAreaService.save(area);
    }

    @Test
    public void getList(){
      List<SysArea> list =   sysAreaService.getList();
      for(SysArea area : list){
          if(area.getParent() != null){
              System.out.printf(area.getParent().getName());
          }
      }
    }

    @Test
    public void getPage(){
        Page<SysArea> page = sysAreaService.getPage(new SysArea(), new PageBounds(0,20));
        System.out.printf(page.getResult().size()+"");
    }



}

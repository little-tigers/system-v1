package sys;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;
import cn.v1.system.pojo.SysMenu;
import cn.v1.system.pojo.SysUser;
import cn.v1.system.service.SysMenuService;
import cn.v1.system.service.SysUserService;
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
public class MenuTest {

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysUserService sysUserService;

    @Test
    public void getById(){
        SysMenu menu = sysMenuService.getById("1009");
        System.out.printf(menu.toString());

    }

    @Test
    public void update(){
        SysMenu menu = sysMenuService.getById("1009");
        sysMenuService.save(menu);
    }

    @Test
    public void getList(){
      List<SysMenu> list =  sysMenuService.getList();
      for(SysMenu menu : list){
          if(menu.getParent() != null){
              System.out.printf(menu.getParent().getName());
          }
      }
    }

    @Test
    public void getPage(){
        Page<SysMenu> page = sysMenuService.getPage(new SysMenu(), new PageBounds(0,20));
        System.out.printf(page.getResult().size()+"");
    }

    @Test
    public void getByRolesList(){
        SysUser user = sysUserService.getById("2");

        List<SysMenu> list = sysMenuService.getByRolesList(user.getRoleList());
        System.out.printf(""+list.size());
    }
}

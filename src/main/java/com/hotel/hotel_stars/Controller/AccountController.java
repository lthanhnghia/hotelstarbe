package com.hotel.hotel_stars.Controller;

import java.util.*;

import com.hotel.hotel_stars.DTO.ApiResponseDto;
import com.hotel.hotel_stars.DTO.StatusResponseDto;
import com.hotel.hotel_stars.DTO.selectDTO.ResponseUsersDTO;
import com.hotel.hotel_stars.Entity.Account;
import com.hotel.hotel_stars.Entity.Booking;
import com.hotel.hotel_stars.Exception.ErrorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.hotel_stars.Config.JwtService;
import com.hotel.hotel_stars.Config.UserInfoService;
import com.hotel.hotel_stars.DTO.AccountDto;
import com.hotel.hotel_stars.DTO.StatusResponseDto;
import com.hotel.hotel_stars.Exception.CustomValidationException;
import com.hotel.hotel_stars.Models.accountModel;
import com.hotel.hotel_stars.Models.changePasswordModel;
import com.hotel.hotel_stars.Repository.AccountRepository;
import com.hotel.hotel_stars.Service.AccountService;
import com.hotel.hotel_stars.Service.TypeRoomService;
import com.hotel.hotel_stars.utils.paramService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/account")
public class AccountController {
    @Autowired
    AccountService accountService;
    @Autowired
    paramService paramServices;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    JwtService jwtService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TypeRoomService typeRoomService;
    @Autowired
    private ErrorsService errorsServices;

    // @GetMapping("/getAll")
    // public ResponseEntity<?> getAllAccounts( @RequestParam (defaultValue = "0",
    // required = false) Integer id) {
    //
    // return ResponseEntity.ok("ạksfasj");
    // }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(accountService.getAccountId(id));
    }

    @PostMapping("/getTokenGG")
    public ResponseEntity<?> getToken(@RequestBody accountModel accountModels) {
        ResponseUsersDTO response = accountService.loginGG(accountModels.getEmail());
        System.out.println(response.getMessage());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAccount(@Valid @RequestBody accountModel accountModels) {
        ApiResponseDto response = accountService.addUser(accountModels);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/loginToken")
    public ResponseEntity<?> loginAccount(@RequestBody accountModel accounts) {
        Map<String, String> response = new HashMap<String, String>();
        String result = accountService.loginSimple(accounts.getUsername(), accounts.getPasswords());
        if (result != null) {
            response = paramServices.messageSuccessApi(200, "success", "Đăng Nhập thành công");
            response.put("token", result);
            return ResponseEntity.ok(response);
        } else {
            response = paramServices.messageSuccessApi(400, "error", "Đăng Nhập thất bại");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/toggleDelete/{id}")
    public ResponseEntity<AccountDto> toggleDeleteStatus(@PathVariable Integer id) {
        AccountDto updatedAccount = accountService.toggleIsDeleteStatus(id);
        return ResponseEntity.ok(updatedAccount);
    }

    @GetMapping("get-info-staff")
    public ResponseEntity<?> getInfoStaff() {
        return ResponseEntity.ok(accountService.getAccountBookings());
    }

    @PostMapping("add-account-staff")
    public ResponseEntity<?> addAccountStaff(@Valid @RequestBody accountModel accountModel) {
        // Kiểm tra lỗi cho thêm mới
        StatusResponseDto statusResponseDto = errorsServices.errorAccountStaff(accountModel, false);
        if (statusResponseDto != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(statusResponseDto);
        }
        AccountDto createdAccount = accountService.AddAccountStaff(accountModel);
        return ResponseEntity.ok(createdAccount);
    }

    @PutMapping("update-account-staff/{id}")
    public ResponseEntity<?> updateAccountStaff(@PathVariable Integer id,
                                                @Valid @RequestBody accountModel accountModel) {
        // Kiểm tra lỗi cho cập nhật
        StatusResponseDto statusResponseDto = errorsServices.errorAccountStaff(accountModel, true);
        if (statusResponseDto != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(statusResponseDto);
        }
        try {
            // Gọi phương thức trong service để cập nhật tài khoản
            AccountDto updatedAccount = accountService.UpdateAccountStaff(id, accountModel);
            return ResponseEntity.ok(updatedAccount); // Trả về tài khoản đã cập nhật
        } catch (CustomValidationException ex) {
            // Trả về lỗi xác thực với danh sách thông báo lỗi
            return ResponseEntity.badRequest().body(ex.getErrorMessages());
        } catch (RuntimeException ex) {
            // Trả về lỗi chung cho các lỗi không xác thực
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra: " + ex.getMessage());
        }
    }


    @DeleteMapping("delete-account-staff/{id}")
    public ResponseEntity<?> deleteAccountStaff(@PathVariable Integer id) {
        try {
            // Gọi phương thức trong service để xóa tài khoản
            accountService.deleteAccountStaff(id);
            return ResponseEntity.ok("Tài khoản đã được xóa thành công."); // Phản hồi thành công
        } catch (NoSuchElementException ex) {
            // Trả về lỗi nếu tài khoản không tồn tại
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tài khoản không tồn tại.");
        } catch (RuntimeException ex) {
            // Trả về lỗi chung cho các lỗi không xác thực
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra: " + ex.getMessage());
        }
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<?> sendEmailEmployee(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        System.out.println(email);
        ApiResponseDto result = accountService.sendEmailUpdatePassword(email);
        System.out.println(result.getCode());
        return ResponseEntity.status(result.getCode()).body(result);
    }

    @GetMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestParam("token") String token) {

        boolean flag = accountService.sendPassword(token);
        System.out.println(flag);
        String generateHtmls = paramServices.generateHtml("Thông Báo", "Mật khẩu thành công vừa gửi qua email của bạn",
                "Mời Bạn quay về login để đăng nhập");

        return ResponseEntity.ok(generateHtmls);
    }

    @PutMapping("/changepassword")
    public ResponseEntity<?> changepass(@RequestBody changePasswordModel changePasswordModels) {
        ApiResponseDto response = accountService.changeUpdatePass(changePasswordModels);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("account-by-id/{username}")
    public ResponseEntity<?> getAccountById(@PathVariable("username") String username) {
        return ResponseEntity.ok(accountService.getAccountInfoByUsername(username));
    }



    @DeleteMapping("/deleteAccount/{id}")
    public StatusResponseDto delete(@PathVariable Integer id) {
        boolean result = accountService.deleteAccountEmployee(id);
        if (result) {
            return new StatusResponseDto("200", "success", "Xóa nhân viên thành công");
        } else {
            return new StatusResponseDto("400", "error", "Nhân viên này đã xác nhận hóa đơn không thể xóa");
        }
    }


    public boolean deleteAccountEmployee(Integer id) {
        try {
            Optional<Account> account = accountRepository.findById(id);

            if (account.isEmpty()) {
                return false;
            }

            accountRepository.deleteById(id);
            return true;

        } catch (DataIntegrityViolationException e) {
            System.out.println("Nhân viên này đã xác nhận hóa đơn không thể xóa");
            return false;
        }
    }

    @PutMapping("/updateAccountCustomer")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody accountModel accountModels) {

        ResponseUsersDTO responseDto = accountService.updateProfileCustomer(accountModels);

        return ResponseEntity.status(responseDto.getCode()).body(responseDto);
    }
}

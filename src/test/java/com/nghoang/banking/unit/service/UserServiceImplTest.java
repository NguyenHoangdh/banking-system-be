package com.nghoang.banking.unit.service;

import com.nghoang.banking.dto.TransactionDto;
import com.nghoang.banking.dto.request.*;
import com.nghoang.banking.dto.response.BankResponse;
import com.nghoang.banking.entity.Role;
import com.nghoang.banking.entity.User;
import com.nghoang.banking.event.event.TransferEmailEvent;
import com.nghoang.banking.event.listener.EmailServiceListener;
import com.nghoang.banking.exception.AppException;
import com.nghoang.banking.exception.ErrorCode;
import com.nghoang.banking.mapper.UserMapper;
import com.nghoang.banking.repository.RoleRepository;
import com.nghoang.banking.repository.UserRepository;
import com.nghoang.banking.service.impl.TransactionService;
import com.nghoang.banking.service.impl.UserServiceImpl;
import com.nghoang.banking.utils.AccountUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    UserMapper userMapper;
    @Mock
    TransactionService transactionService;
    @Mock
    RoleRepository roleRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    UserServiceImpl userService;
    UserRequest createUserRequest;
    User user;
    User destinationUser;
    CreditRequest creditRequest;
    DebitRequest debitRequest;
    TransferRequest transferRequest;
    UserUpdateRequest userUpdateRequest;
    EnquiryRequest enquiryRequest;

    @BeforeEach
    void setUp() {
        createUserRequest = UserRequest.builder()
                .email("lea@gmail.com")
                .firstName("A")
                .lastName("Lê")
                .password("123456789")
                .build();
        userUpdateRequest = UserUpdateRequest.builder()
                .email("lea@gmail.com")
                .address("New York")
                .build();
        user = User.builder()
                .email("lea@gmail.com")
                .firstName("A")
                .lastName("Lê")
                .accountBalance(BigDecimal.ZERO)
                .password("123456789")
                .status("ACTIVE")
                .accountNumber("")
                .build();
        destinationUser = User.builder()
                .accountNumber("2026123123")
                .password("123123123")
                .status("ACTIVE")
                .build();

        creditRequest = CreditRequest.builder()
                .amount(BigDecimal.valueOf(10000))
                .accountNumber("")
                .build();

        debitRequest = DebitRequest.builder()
                .accountNumber(AccountUtils.generateAccountNumber())
                .amount(BigDecimal.ZERO)
                .build();

        transferRequest = TransferRequest.builder()
                .destinationAccountNumber("")
                .amount(BigDecimal.ZERO)
                .build();
        enquiryRequest = EnquiryRequest.builder()
                .accountNumber("")
                .build();
    }
    @Test
    void test_nameEnquire_userNotExisted() {
        when(userRepository.findByAccountNumber(enquiryRequest.getAccountNumber())).thenReturn(Optional.empty());
        AppException appException = assertThrows(AppException.class, () -> userService.nameEnquiry(enquiryRequest));

        assertEquals(ErrorCode.USER_NOT_EXISTED, appException.getErrorCode());

        verify(userRepository, times(1)).findByAccountNumber(enquiryRequest.getAccountNumber());
    }
    @Test
    void test_nameEnquire_accountNotActive() {
        user.setStatus("INACTIVE");
        when(userRepository.findByAccountNumber(enquiryRequest.getAccountNumber())).thenReturn(Optional.of(user));
        AppException appException = assertThrows(AppException.class, () -> userService.nameEnquiry(enquiryRequest));

        assertEquals(ErrorCode.ACCOUNT_NOT_ACTIVE, appException.getErrorCode());

        verify(userRepository, times(1)).findByAccountNumber(enquiryRequest.getAccountNumber());
    }
    @Test
    void test_nameEnquire_success() {
        when(userRepository.findByAccountNumber(enquiryRequest.getAccountNumber())).thenReturn(Optional.of(user));

        BankResponse bankResponse = userService.nameEnquiry(enquiryRequest);

        assertEquals(AccountUtils.ACCOUNT_FOUND_CODE, bankResponse.getCode());
        assertEquals("A Lê", bankResponse.getAccountInfo().getAccountName());
        verify(userRepository, times(1)).findByAccountNumber(enquiryRequest.getAccountNumber());
    }

    @Test
    void test_createAccount_shouldThrowException() {

        //thiết lập dữ liệu và mô phỏng hành vi mock email đã tồn tại - Given phase
        when(userRepository.existsByEmailIs("lea@gmail.com")).thenReturn(true);

        //gọi hàm thật, bắt exception được ném ra - WHEN phase
        AppException exception = assertThrows(AppException.class, () -> userService.createAccount(createUserRequest)); //hàm thật userService.createAccount() sẽ chạy trước khi assertThrows kịp bắt AppException nên phải thêm lambda expression vào để trì hoãn hàm createAccount tạo đk cho assertThrows kịp bắt exception

        //kiểm tra đúng loại lỗi - THEN phase
        assertEquals(ErrorCode.ACCOUNT_EXISTED, exception.getErrorCode());

        //đảm bảo không có side-effect nào xảy ra sau khi exception ném ra - VERIFY
        verify(userRepository, times(1)).existsByEmailIs("lea@gmail.com");
        verify(userMapper, never()).toUser(any());
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void test_createAccount_success() {
        //GIVEN
        Role userRole = Role.builder().name("USER").build();

        when(userRepository.existsByEmailIs(createUserRequest.getEmail())).thenReturn(false);
        when(userMapper.toUser(createUserRequest)).thenReturn(user);
        when(userRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(roleRepository.findById("USER")).thenReturn(Optional.of(userRole));
//        when(passwordEncoder.encode("123456789")).thenReturn("encoded_password");
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        //WHEN
        BankResponse bankResponse = userService.createAccount(createUserRequest);

        //THEN
        assertEquals(AccountUtils.ACCOUNT_CREATION_SUCCESS, bankResponse.getCode());
        assertEquals("A Lê", bankResponse.getAccountInfo().getAccountName());

        //VERIFY
        verify(userRepository, times(1)).existsByEmailIs("lea@gmail.com");
        verify(userRepository, times(1)).existsByAccountNumber(anyString());
        verify(userMapper, times(1)).toUser(createUserRequest);
        verify(roleRepository, times(1)).findById("USER");
        verify(passwordEncoder, times(1)).encode("123456789");
        verify(userRepository, times(1)).save(any(User.class));
    }


    @Test
    void test_createAccount_roleNotExisted() {
        when(userRepository.existsByEmailIs(createUserRequest.getEmail())).thenReturn(false);
        when(roleRepository.findById("USER")).thenReturn(Optional.empty());
        when(userMapper.toUser(createUserRequest)).thenReturn(user);

        AppException appException = assertThrows(AppException.class, () -> userService.createAccount(createUserRequest));

        assertEquals(ErrorCode.ROLE_NOT_EXISTED, appException.getErrorCode());

        verify(userRepository, times(1)).existsByEmailIs(createUserRequest.getEmail());
        verify(roleRepository, times(1)).findById("USER");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, times(1)).encode(any());
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void test_createAccount_accountNumberExisted() {
        Role userRole = Role.builder().name("USER").build();

        when(userRepository.existsByEmailIs(createUserRequest.getEmail())).thenReturn(false);
        when(userMapper.toUser(createUserRequest)).thenReturn(user);
        when(userRepository.existsByAccountNumber(anyString())).thenReturn(true, false);
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("123123123");
        when(roleRepository.findById("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(user)).thenReturn(user);

        BankResponse bankResponse = userService.createAccount(createUserRequest);
        assertEquals(AccountUtils.ACCOUNT_CREATION_SUCCESS, bankResponse.getCode());
        assertNotNull(bankResponse.getAccountInfo().getAccountNumber());

        verify(userRepository, times(1)).existsByEmailIs("lea@gmail.com");
        verify(userRepository, times(2)).existsByAccountNumber(anyString());
        verify(userMapper, times(1)).toUser(createUserRequest);
        verify(roleRepository, times(1)).findById("USER");
        verify(passwordEncoder, times(1)).encode(createUserRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void test_updateAccount_userNotExist() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userUpdateRequest.getEmail());
        when(userRepository.findUserByEmailWithLock(userUpdateRequest.getEmail())).thenReturn(Optional.empty());

        AppException appException = assertThrows(AppException.class,() -> userService.updateAccount(userUpdateRequest));

        assertEquals(ErrorCode.USER_NOT_EXISTED, appException.getErrorCode());

        verify(userRepository, times(1)).findUserByEmailWithLock(userUpdateRequest.getEmail());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).updateUser(any(), any());
        verify(applicationEventPublisher, never()).publishEvent(any());

    }

    @Test
    void test_updateAccount_success() {
        userUpdateRequest.setPassword("123123123");
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userUpdateRequest.getEmail());
        when(userRepository.findUserByEmailWithLock(userUpdateRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(userUpdateRequest.getPassword())).thenReturn("123123123");
        when(userRepository.save(user)).thenReturn(user);

        BankResponse bankResponse = userService.updateAccount(userUpdateRequest);

        assertEquals(AccountUtils.UPDATE_USER_SUCCESSFUL_CODE, bankResponse.getCode());
        assertEquals("A Lê", bankResponse.getAccountInfo().getAccountName());

        verify(userRepository, times(1)).findUserByEmailWithLock(userUpdateRequest.getEmail());
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).updateUser(user, userUpdateRequest);
        verify(passwordEncoder, times(1)).encode(userUpdateRequest.getPassword());
    }
    @Test
    void test_updateAccount_successWithPasswordNull() {
        user.setPassword("123123aaa");
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userUpdateRequest.getEmail());
        when(userRepository.findUserByEmailWithLock("lea@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        BankResponse bankResponse = userService.updateAccount(userUpdateRequest);

        assertEquals(AccountUtils.UPDATE_USER_SUCCESSFUL_CODE, bankResponse.getCode());
        assertEquals("A Lê", bankResponse.getAccountInfo().getAccountName());
        assertEquals("123123aaa", user.getPassword());
        verify(userRepository, times(1)).findUserByEmailWithLock("lea@gmail.com");
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).updateUser(user, userUpdateRequest);
        verify(passwordEncoder, never()).encode(any());
    }
    @Test
    void test_updateAccount_successWithPasswordBlank() {
        user.setPassword("123123aaa");
        userUpdateRequest.setPassword("");
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userUpdateRequest.getEmail());
        when(userRepository.findUserByEmailWithLock("lea@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        BankResponse bankResponse = userService.updateAccount(userUpdateRequest);

        assertEquals(AccountUtils.UPDATE_USER_SUCCESSFUL_CODE, bankResponse.getCode());
        assertEquals("A Lê", bankResponse.getAccountInfo().getAccountName());
        assertEquals("123123aaa", user.getPassword());
        verify(userRepository, times(1)).findUserByEmailWithLock("lea@gmail.com");
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).updateUser(user, userUpdateRequest);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void test_balanceEnquire_shouldThrowException() {
        user.setStatus("INACTIVE");

//        when(mockUser.getStatus()).thenReturn("INACTIVE");
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lea@gmail.com");

        when(userRepository.findUserByEmail("lea@gmail.com")).thenReturn(Optional.of(user));

        AppException exception = assertThrows(AppException.class, () -> userService.balanceEnquiry());

        assertEquals(ErrorCode.ACCOUNT_NOT_ACTIVE, exception.getErrorCode());

        verify(userRepository, times(1)).findUserByEmail("lea@gmail.com");
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void test_balanceEnquire_userNotExisted() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lea@gmail.com");
        when(userRepository.findUserByEmail("lea@gmail.com")).thenReturn(Optional.empty());

        AppException appException = assertThrows(AppException.class, () -> userService.balanceEnquiry());

        assertEquals(ErrorCode.USER_NOT_EXISTED, appException.getErrorCode());

        verify(userRepository, times(1)).findUserByEmail("lea@gmail.com");
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
    @Test
    void test_balanceEnquire_success() {
        user.setStatus("ACTIVE");
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lea@gmail.com");
        when(userRepository.findUserByEmail("lea@gmail.com")).thenReturn(Optional.of(user));

        BankResponse response = userService.balanceEnquiry();

        assertEquals(AccountUtils.ACCOUNT_FOUND_CODE, response.getCode());
        assertEquals("A Lê", response.getAccountInfo().getAccountName());

        verify(userRepository, times(1)).findUserByEmail("lea@gmail.com");
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void test_credit_accountNotActive() {
        user.setStatus("INACTIVE");
        when(userRepository.findByAccountNumberWithLock(creditRequest.getAccountNumber())).thenReturn(Optional.of(user));
//        when(userService.credit(creditRequest)).thenReturn()

        AppException appException = assertThrows(AppException.class, () -> userService.credit(creditRequest));
        assertEquals(ErrorCode.ACCOUNT_NOT_ACTIVE, appException.getErrorCode());

        verify(userRepository, times(1)).findByAccountNumberWithLock(creditRequest.getAccountNumber());
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(transactionService, never()).saveTransaction(any());
    }

    @Test
    void test_credit_accountNotFound() {
        creditRequest.setAccountNumber("1234562026");

        when(userRepository.findByAccountNumberWithLock(creditRequest.getAccountNumber())).thenReturn(Optional.empty());

        AppException appException = assertThrows(AppException.class, () -> userService.credit(creditRequest));

        assertEquals(ErrorCode.USER_NOT_EXISTED, appException.getErrorCode());

        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(userRepository, never()).save(any());
        verify(userRepository, times(1)).findByAccountNumberWithLock(anyString());
        verify(transactionService, never()).saveTransaction(any());
    }

    @Test
    void test_credit_verifyNoSideEffectsOnFailure() {
        // GIVEN - tài khoản hợp lệ, nhưng save() ném exception giữa chừng
        user.setStatus("ACTIVE");
        user.setAccountBalance(BigDecimal.ZERO);
        when(userRepository.findByAccountNumberWithLock(creditRequest.getAccountNumber())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        // WHEN
        assertThrows(RuntimeException.class, () -> userService.credit(creditRequest));

        // VERIFY - saveTransaction và publishEvent không được gọi khi save thất bại
        verify(userRepository, times(1)).save(any(User.class));
        verify(transactionService, never()).saveTransaction(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void test_credit_success() {
        user.setStatus("ACTIVE");
        creditRequest.setAccountNumber(AccountUtils.generateAccountNumber());

        user.setAccountNumber(creditRequest.getAccountNumber());
        when(userRepository.findByAccountNumberWithLock(creditRequest.getAccountNumber())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        TransactionDto transactionDto = TransactionDto.builder()
                .status("SUCCESS")
                .amount(creditRequest.getAmount())
                .accountNumber(creditRequest.getAccountNumber())
                .transactionType("CREDIT")
                .build();
        BankResponse bankResponse = userService.credit(creditRequest);

        assertEquals(AccountUtils.ACCOUNT_CREDITED_SUCCESS, bankResponse.getCode());
        assertEquals(BigDecimal.valueOf(10000), bankResponse.getAccountInfo().getAccountBalance());

        verify(userRepository, times(1)).findByAccountNumberWithLock(creditRequest.getAccountNumber());
        verify(userRepository, times(1)).save(any(User.class));
        verify(transactionService, times(1)).saveTransaction(transactionDto);
    }

    @Test
    void test_debit_accountNotFound() {
        debitRequest.setAccountNumber("1234562026");

        when(userRepository.findByAccountNumberWithLock(debitRequest.getAccountNumber())).thenReturn(Optional.empty());

        AppException appException = assertThrows(AppException.class, () -> userService.debit(debitRequest));

        assertEquals(ErrorCode.USER_NOT_EXISTED, appException.getErrorCode());

        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(userRepository, never()).save(any());
        verify(userRepository, times(1)).findByAccountNumberWithLock(anyString());
        verify(transactionService, never()).saveTransaction(any());
    }

    @Test
    void test_debit_accountNotActive() {
        // GIVEN
        user.setStatus("INACTIVE");

        when(userRepository.findByAccountNumberWithLock(debitRequest.getAccountNumber())).thenReturn(Optional.of(user));

        // WHEN
        AppException appException = assertThrows(AppException.class, () -> userService.debit(debitRequest));

        assertEquals(ErrorCode.ACCOUNT_NOT_ACTIVE, appException.getErrorCode());

        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(userRepository, never()).save(any());
        verify(userRepository, times(1)).findByAccountNumberWithLock(debitRequest.getAccountNumber());
        verify(transactionService, never()).saveTransaction(any());
    }

    @Test
    void test_debit_insufficientBalance() {
        user.setStatus("ACTIVE");
        debitRequest.setAmount(BigDecimal.valueOf(50000));
        user.setAccountBalance(BigDecimal.valueOf(10000));
        when(userRepository.findByAccountNumberWithLock(debitRequest.getAccountNumber())).thenReturn(Optional.of(user));

        AppException exception = assertThrows(AppException.class, () -> userService.debit(debitRequest));

        assertEquals(ErrorCode.INSUFFICIENT_BALANCE_CODE, exception.getErrorCode());

        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(userRepository, times(1)).findByAccountNumberWithLock(debitRequest.getAccountNumber());
        verify(transactionService, never()).saveTransaction(any());
    }

    @Test
    void test_debit_success() {
        debitRequest.setAccountNumber(AccountUtils.generateAccountNumber());
        user.setAccountBalance(BigDecimal.valueOf(50000));
        debitRequest.setAmount(BigDecimal.valueOf(10000));
        user.setAccountNumber(debitRequest.getAccountNumber());
        when(userRepository.findByAccountNumberWithLock(debitRequest.getAccountNumber())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        TransactionDto transactionDto = TransactionDto.builder()
                .status("SUCCESS")
                .amount(debitRequest.getAmount())
                .accountNumber(debitRequest.getAccountNumber())
                .transactionType("DEBIT")
                .build();
        BankResponse bankResponse = userService.debit(debitRequest);

        assertEquals(AccountUtils.ACCOUNT_DEBITED_SUCCESS, bankResponse.getCode());
        assertEquals(BigDecimal.valueOf(40000), bankResponse.getAccountInfo().getAccountBalance());

        verify(userRepository, times(1)).findByAccountNumberWithLock(debitRequest.getAccountNumber());
        verify(userRepository, times(1)).save(any(User.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(EmailServiceListener.class));
        verify(transactionService, times(1)).saveTransaction(transactionDto);
    }

    @Test
    void test_debit_verifyNoSideEffectsOnFailure() {
        // GIVEN - tài khoản hợp lệ, nhưng save() ném exception giữa chừng
        user.setAccountBalance(BigDecimal.valueOf(50000));
        debitRequest.setAmount(BigDecimal.valueOf(10000));
        when(userRepository.findByAccountNumberWithLock(debitRequest.getAccountNumber())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        // WHEN
        assertThrows(RuntimeException.class, () -> userService.debit(debitRequest));

        // VERIFY - saveTransaction và publishEvent không được gọi khi save thất bại
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).findByAccountNumberWithLock(debitRequest.getAccountNumber());
        verify(transactionService, never()).saveTransaction(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void test_transfer_sourceNotFound() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lea@gmail.com");
        when(userRepository.findUserByEmailWithLock("lea@gmail.com")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->userService.transfer(transferRequest));

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());

        verify(transactionService, never()).saveTransaction(any());
        verify(userRepository, never()).save(any());
        verify(userRepository, times(1)).findUserByEmailWithLock("lea@gmail.com");
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void test_transfer_sourceNotActive() {
        user.setStatus("INACTIVE");
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lea@gmail.com");
        when(userRepository.findUserByEmailWithLock("lea@gmail.com")).thenReturn(Optional.of(user));

        AppException appException = assertThrows(AppException.class, () -> userService.transfer(transferRequest));

        assertEquals(ErrorCode.ACCOUNT_NOT_ACTIVE, appException.getErrorCode());

        verify(userRepository, times(1)).findUserByEmailWithLock("lea@gmail.com");
        verify(userRepository, never()).findByAccountNumberWithLock(any()); //check method transfer dừng lại sớm khi source ko active
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(transactionService, never()).saveTransaction(any());
    }

    @Test
    void test_transfer_selfTransfer() {
        user.setAccountNumber("2026123123");
        transferRequest.setDestinationAccountNumber(user.getAccountNumber());
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lea@gmail.com");
        when(userRepository.findUserByEmailWithLock("lea@gmail.com")).thenReturn(Optional.of(user));

        AppException appException = assertThrows(AppException.class, () -> userService.transfer(transferRequest));

        assertEquals(ErrorCode.SELF_TRANSFER_NOT_ALLOWED, appException.getErrorCode());

        verify(userRepository, times(1)).findUserByEmailWithLock("lea@gmail.com");
        verify(userRepository, never()).findByAccountNumberWithLock(any());
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(transactionService, never()).saveTransaction(any());

    }

    @Test
    void test_transfer_balanceNotEnough() {
        user.setAccountBalance(BigDecimal.valueOf(10000));
        transferRequest.setAmount(BigDecimal.valueOf(50000));
        transferRequest.setDestinationAccountNumber("2026123333");
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lea@gmail.com");
        when(userRepository.findUserByEmailWithLock("lea@gmail.com")).thenReturn(Optional.of(user));

        AppException appException = assertThrows(AppException.class, () -> userService.transfer(transferRequest));
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE_CODE, appException.getErrorCode());

        verify(userRepository, times(1)).findUserByEmailWithLock("lea@gmail.com");
        verify(userRepository, never()).findByAccountNumberWithLock(transferRequest.getDestinationAccountNumber());
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(transactionService, never()).saveTransaction(any());
    }

    @Test
    void test_transfer_destinationAccountNotExist() {
        user.setAccountBalance(BigDecimal.valueOf(20000));
        transferRequest.setAmount(BigDecimal.valueOf(10000));
        transferRequest.setDestinationAccountNumber("2026123123");
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lea@gmail.com");
        when(userRepository.findUserByEmailWithLock("lea@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.findByAccountNumberWithLock(transferRequest.getDestinationAccountNumber())).thenReturn(Optional.empty());

        AppException appException = assertThrows(AppException.class, () -> userService.transfer(transferRequest));

        assertEquals(ErrorCode.DESTINATION_ACCOUNT_NOT_EXISTED, appException.getErrorCode());

        verify(userRepository, times(1)).findUserByEmailWithLock("lea@gmail.com");
        verify(userRepository, times(1)).findByAccountNumberWithLock(transferRequest.getDestinationAccountNumber());
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(transactionService, never()).saveTransaction(any());

    }

    @Test
    void test_transfer_destinationAccountNotActive() {
        destinationUser.setStatus("INACTIVE");
        user.setAccountBalance(BigDecimal.valueOf(20000));
        transferRequest.setAmount(BigDecimal.valueOf(10000));
        transferRequest.setDestinationAccountNumber("2026123123");
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lea@gmail.com");
        when(userRepository.findUserByEmailWithLock("lea@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.findByAccountNumberWithLock(transferRequest.getDestinationAccountNumber())).thenReturn(Optional.of(destinationUser));

        AppException appException = assertThrows(AppException.class, () -> userService.transfer(transferRequest));

        assertEquals(ErrorCode.ACCOUNT_NOT_ACTIVE, appException.getErrorCode());

        verify(userRepository, times(1)).findUserByEmailWithLock("lea@gmail.com");
        verify(userRepository, times(1)).findByAccountNumberWithLock(transferRequest.getDestinationAccountNumber());
        verify(userRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(transactionService, never()).saveTransaction(any());
    }

    @Test
    void test_transfer_success() {
        destinationUser.setStatus("ACTIVE");
        destinationUser.setAccountBalance(BigDecimal.valueOf(20000));
        user.setAccountBalance(BigDecimal.valueOf(20000));
        transferRequest.setAmount(BigDecimal.valueOf(10000));
        transferRequest.setDestinationAccountNumber("2026123123");
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lea@gmail.com");
        when(userRepository.findUserByEmailWithLock("lea@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.findByAccountNumberWithLock(transferRequest.getDestinationAccountNumber())).thenReturn(Optional.of(destinationUser));

        BankResponse bankResponse = userService.transfer(transferRequest);

        assertEquals(AccountUtils.TRANSFER_SUCCESSFUL_CODE, bankResponse.getCode());
        assertEquals(BigDecimal.valueOf(10000), bankResponse.getAccountInfo().getAccountBalance());
        assertEquals(BigDecimal.valueOf(30000), destinationUser.getAccountBalance());

        verify(userRepository, times(1)).findUserByEmailWithLock("lea@gmail.com");
        verify(userRepository, times(1)).findByAccountNumberWithLock(transferRequest.getDestinationAccountNumber());
        verify(userRepository, times(2)).save(any(User.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(TransferEmailEvent.class));
        verify(transactionService, times(2)).saveTransaction(any(TransactionDto.class));
    }

    @Test
    void test_transfer_verifyNoSideEffectsOnFailure() {
        // GIVEN - tài khoản hợp lệ, nhưng save() ném exception giữa chừng
        user.setAccountBalance(BigDecimal.valueOf(50000));
        transferRequest.setDestinationAccountNumber("202513211");
        transferRequest.setAmount(BigDecimal.valueOf(10000));
        destinationUser.setAccountBalance(BigDecimal.valueOf(20000));
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("lea@gmail.com");
        when(userRepository.findUserByEmailWithLock("lea@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.findByAccountNumberWithLock(transferRequest.getDestinationAccountNumber())).thenReturn(Optional.of(destinationUser));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        // WHEN
        assertThrows(RuntimeException.class, () -> userService.transfer(transferRequest));


        // VERIFY - saveTransaction và publishEvent không được gọi khi save thất bại
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).findUserByEmailWithLock("lea@gmail.com");
        verify(userRepository, times(1)).findByAccountNumberWithLock(transferRequest.getDestinationAccountNumber());
        verify(transactionService, never()).saveTransaction(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
